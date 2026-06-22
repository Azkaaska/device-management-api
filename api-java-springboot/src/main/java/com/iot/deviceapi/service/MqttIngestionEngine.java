package com.iot.deviceapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.model.ReadingKey;
import com.iot.deviceapi.repository.DeviceRepository;
import com.iot.deviceapi.repository.ReadingRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
public class MqttIngestionEngine {

    @Value("${MQTT_HOST:127.0.0.1}")
    private String mqttHost;

    @Value("${MQTT_PORT:1883}")
    private int mqttPort;

    private final DeviceRepository deviceRepository;
    private final ReadingRepository readingRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ZoneId localZone = ZoneId.of("GMT+7");
    
    private MqttClient client;

    public MqttIngestionEngine(DeviceRepository deviceRepository, ReadingRepository readingRepository) {
        this.deviceRepository = deviceRepository;
        this.readingRepository = readingRepository;
    }

    @PostConstruct
    public void init() {
        System.out.println("[MQTT INGESTION] Initializing MQTT Ingestion Service thread...");
        Thread subscriberThread = new Thread(this::connectionLoop);
        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }

    private void connectionLoop() {
        String brokerUrl = "tcp://" + mqttHost + ":" + mqttPort;
        String clientId = "SpringBootMqttSubscriber";

        while (!Thread.currentThread().isInterrupted()) {
            try {
                System.out.println("[MQTT INGESTION] Attempting to connect to broker: " + brokerUrl);
                client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                options.setAutomaticReconnect(true);

                client.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        String status = reconnect ? "Reconnected" : "Connected";
                        System.out.println("[MQTT INGESTION] " + status + " to broker: " + serverURI);
                        try {
                            client.subscribe("buildingA/+/+", 1);
                            System.out.println("[MQTT INGESTION] Successfully subscribed to topic: buildingA/+/+");
                        } catch (MqttException e) {
                            System.err.println("[MQTT INGESTION] Subscription failed: " + e.getMessage());
                        }
                    }

                    @Override
                    public void connectionLost(Throwable cause) {
                        System.err.println("[MQTT INGESTION] Connection lost to broker. Automatic reconnect triggered...");
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        processPacket(topic, message);
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {}
                });

                client.connect(options);
                break;

            } catch (MqttException e) {
                System.err.println("[MQTT INGESTION] Connection failed: " + e.getMessage() + ". Retrying in 5s...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void processPacket(String topic, MqttMessage message) {
        try {
            String[] parts = topic.split("/");
            if (parts.length != 3) {
                System.out.println("[MQTT INGESTION] Ignored invalid topic structure: " + topic);
                return;
            }

            UUID deviceId = UUID.fromString(parts[2]);
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), new TypeReference<Map<String, Object>>() {});

            if (payload == null || !payload.containsKey("ts") || !payload.containsKey("temperature") || !payload.containsKey("humidity")) {
                System.out.println("[MQTT INGESTION] Malformed payload structure on topic: " + topic);
                return;
            }

            if (!deviceRepository.existsById(deviceId)) {
                System.out.println("[MQTT INGESTION] Rejected telemetry. Device ID matching " + deviceId + " does not exist in local database.");
                return;
            }

            long tsDevice = ((Number) payload.get("ts")).longValue();
            float temperature = ((Number) payload.get("temperature")).floatValue();
            float humidity = ((Number) payload.get("humidity")).floatValue();
            
            long tsReceive = System.currentTimeMillis();
            String bucketDate = Instant.ofEpochMilli(tsReceive).atZone(localZone).format(formatter);

            ReadingKey key = new ReadingKey(deviceId, bucketDate, tsDevice);
            Reading reading = new Reading();
            reading.setKey(key);
            reading.setTsReceive(tsReceive);
            reading.setTemperature(temperature);
            reading.setHumidity(humidity);
            
            readingRepository.save(reading);
            
            System.out.println("[MQTT INGESTION] [SAVED] Device: " + deviceId + " | Bucket: " + bucketDate + " | Temp: " + temperature + "°C | Humid: " + humidity + "%");
            
        } catch (Exception e) {
            System.err.println("[MQTT INGESTION] Processing Error: " + e.getMessage());
        }
    }

    @PreDestroy
    public void cleanUpMqttResources() {
        if (client != null) {
            try {
                System.out.println("[MQTT INGESTION] Shutting down connection engine cleanly...");
                if (client.isConnected()) client.disconnect();
                client.close();
            } catch (MqttException e) {
                System.err.println("[MQTT INGESTION] Error closing resource hooks: " + e.getMessage());
            }
        }
    }
}
