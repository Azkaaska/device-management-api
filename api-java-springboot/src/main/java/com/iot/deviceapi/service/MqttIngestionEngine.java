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
        Thread subscriberThread = new Thread(this::connectionLoop);
        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }

    private void connectionLoop() {
        String brokerUrl = "tcp://" + mqttHost + ":" + mqttPort;
        String clientId = "SpringBootMqttSubscriber";

        while (!Thread.currentThread().isInterrupted()) {
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
                        System.err.println("MQTT Ingestion: Broker connection lost!");
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        processPacket(topic, message);
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {}
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
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processPacket(String topic, MqttMessage message) {
        try {
            String[] parts = topic.split("/");
            if (parts.length != 3) return;

            UUID deviceId = UUID.fromString(parts[2]);
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), new TypeReference<Map<String, Object>>() {});

            if (payload == null || !payload.containsKey("ts") || !payload.containsKey("sensor_values")) return;

            long ts = ((Number) payload.get("ts")).longValue();
            Map<String, Object> sensorValues = (Map<String, Object>) payload.get("sensor_values");

            if (!deviceRepository.existsById(deviceId)) {
                System.out.println("MQTT Ingestion: Unknown device " + deviceId + ", dropping packet.");
                return;
            }

            String bucketDate = Instant.ofEpochMilli(ts).atZone(localZone).format(formatter);
            ReadingKey key = new ReadingKey(deviceId, bucketDate, ts);
            Reading reading = new Reading();
            reading.setKey(key);
            reading.setSensorValues(sensorValues);
            
            readingRepository.save(reading);
        } catch (Exception e) {
            System.err.println("MQTT Ingestion: Error processing message - " + e.getMessage());
        }
    }

    @PreDestroy
    public void cleanUpMqttResources() {
        if (client != null) {
            try {
                if (client.isConnected()) {
                    client.disconnect();
                }
                client.close();
            } catch (MqttException e) {
                System.err.println("Error closing MQTT Client: " + e.getMessage());
            }
        }
    }
}
