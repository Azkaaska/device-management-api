package com.iot.deviceapi.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.model.ReadingKey;
import com.iot.deviceapi.repository.DeviceRepository;
import com.iot.deviceapi.repository.ReadingRepository;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Component
public class MqttConfig implements CommandLineRunner {

    @Value("${MQTT_HOST:127.0.0.1}")
    private String mqttHost;

    @Value("${MQTT_PORT:1883}")
    private int mqttPort;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ReadingRepository readingRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ZoneId localZone = ZoneId.of("GMT+7");

    private MqttClient client;

    @Override
    public void run(String... args) {
        Thread subscriberThread = new Thread(() -> {
            String brokerUrl = "tcp://" + mqttHost + ":" + mqttPort;
            String clientId = "SpringBootMqttSubscriber";

            while (true) {
                try {
                    client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setCleanSession(true);
                    options.setAutomaticReconnect(true);

                    client.setCallback(new MqttCallbackExtended() {
                        @Override
                        public void connectComplete(boolean reconnect, String serverURI) {
                            System.out.println("MQTT Ingestion: Connected to broker " + serverURI);
                            try {
                                client.subscribe("+/+/+", 1);
                                System.out.println("MQTT Ingestion: Successfully subscribed to topics (+/+/+)");
                            } catch (MqttException e) {
                                System.err.println("MQTT Ingestion: Failed to subscribe: " + e.getMessage());
                            }
                        }

                        @Override
                        public void connectionLost(Throwable cause) {
                            System.err.println("MQTT Ingestion: Broker connection lost! Cause: " + 
                                    (cause != null ? cause.getMessage() : "Unknown"));
                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        public void messageArrived(String topic, MqttMessage message) {
                            try {
                                String[] parts = topic.split("/");
                                if (parts.length != 3) {
                                    System.err.println("[Parsing Drop] Invalid topic structure: " + topic);
                                    return;
                                }

                                UUID deviceId;
                                try {
                                    deviceId = UUID.fromString(parts[2]);
                                } catch (IllegalArgumentException e) {
                                    System.err.println("[Parsing Drop] Token is not a valid UUID: " + parts[2]);
                                    return;
                                }

                                Map<String, Object> payload;
                                try {
                                    payload = objectMapper.readValue(message.getPayload(), new TypeReference<Map<String, Object>>() {});
                                } catch (Exception e) {
                                    System.err.println("[Parsing Drop] Corrupted or non-JSON payload received: " + e.getMessage());
                                    return;
                                }

                                if (payload == null || !payload.containsKey("ts") || !payload.containsKey("sensor_values")) {
                                    System.err.println("[Parsing Drop] Missing mandatory 'ts' or 'sensor_values' field");
                                    return;
                                }

                                long ts = ((Number) payload.get("ts")).longValue();
                                Map<String, Object> sensorValues = (Map<String, Object>) payload.get("sensor_values");

                                boolean exists = deviceRepository.existsById(deviceId);
                                if (!exists) {
                                    System.out.println("MQTT Ingestion: Unknown device " + deviceId + ", skipping save execution");
                                    return;
                                }

                                String bucketDate = Instant.ofEpochMilli(ts).atZone(localZone).format(formatter);
                                ReadingKey key = new ReadingKey(deviceId, bucketDate, ts);
                                Reading reading = new Reading();
                                reading.setKey(key);
                                reading.setSensorValues(sensorValues);
                                
                                readingRepository.save(reading);
                                System.out.println("MQTT Ingestion: Saved reading telemetry for device " + deviceId);

                            } catch (Exception e) {
                                System.err.println("MQTT Ingestion: Critical exception handling packet: " + e.getMessage());
                            }
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                        }
                    });

                    System.out.println("MQTT Ingestion: Initiating connection to " + brokerUrl);
                    client.connect(options);
                    break;

                } catch (MqttException e) {
                    System.err.println("MQTT Ingestion: Broker unreachable: " + e.getMessage() + ". Retrying in 5 seconds...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
        
        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }

    @PreDestroy
    public void cleanUpMqttResources() {
        System.out.println("Spring Boot Shuts Down: Cleaning up MQTT Subscriber resources safely...");
        if (client != null) {
            try {
                if (client.isConnected()) {
                    client.disconnect();
                }
                client.close();
                System.out.println("MQTT Ingestion Client closed gracefully. No leaks left.");
            } catch (MqttException e) {
                System.err.println("Error while closing MQTT Subscriber during teardown: " + e.getMessage());
            }
        }
    }
}
