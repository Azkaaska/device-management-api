package com.iot.deviceapi.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.model.ReadingKey;
import com.iot.deviceapi.repository.DeviceRepository;
import com.iot.deviceapi.repository.ReadingRepository;
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

    @Override
    public void run(String... args) {
        String brokerUrl = "tcp://" + mqttHost + ":" + mqttPort;
        String clientId = "SpringBootMqttSubscriber";

        try {
            try (MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence())) {
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                options.setAutomaticReconnect(true);

                client.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        System.out.println("MQTT Ingestion: Connected to broker " + serverURI);
                        try {
                            // topic: {place1}/{place2}/{deviceId}
                            client.subscribe("+/+/+", 1);
                        } catch (MqttException e) {
                            System.err.println("MQTT Ingestion: Failed to subscribe: " + e.getMessage());
                        }
                    }

                    @Override
                    public void connectionLost(Throwable cause) {
                        System.out.println("MQTT Ingestion: Connection lost: " + cause.getMessage());
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        try {
                            String[] parts = topic.split("/");
                            // expect: place1 / place2 / deviceId
                            if (parts.length != 3) return;
                            UUID deviceId = UUID.fromString(parts[2]);

                            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), new TypeReference<Map<String, Object>>() {});

                            long ts = payload.containsKey("ts")
                                ? ((Number) payload.get("ts")).longValue()
                                : System.currentTimeMillis();

                            Map<String, Object> sensorValues = payload.containsKey("sensor_values")
                                ? (Map<String, Object>) payload.get("sensor_values")
                                : Map.of();

                            boolean exists = deviceRepository.existsById(deviceId);
                            if (!exists) {
                                System.out.println("MQTT Ingestion: Unknown device " + deviceId + ", skipping");
                                return;
                            }

                            String bucketDate = Instant.ofEpochMilli(ts).atZone(localZone).format(formatter);
                            ReadingKey key = new ReadingKey(deviceId, bucketDate, ts);
                            Reading reading = new Reading();
                            reading.setKey(key);
                            reading.setSensorValues(sensorValues);
                            readingRepository.save(reading);
                            System.out.println("MQTT Ingestion: Saved reading for device " + deviceId);

                        } catch (Exception e) {
                            System.err.println("MQTT Ingestion: Error processing message: " + e.getMessage());
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                    }
                });

                client.connect(options);
            }

        } catch (MqttException e) {
            System.err.println("MQTT Ingestion: Failed to start MQTT client: " + e.getMessage());
        }
    }
}
