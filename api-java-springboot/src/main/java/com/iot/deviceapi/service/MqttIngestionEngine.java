package com.iot.deviceapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.deviceapi.handler.SensorWebSocketHandler;
import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.model.ReadingKey;
import com.iot.deviceapi.model.WebSocketEvent;
import com.iot.deviceapi.repository.DeviceRepository;
import com.iot.deviceapi.repository.ReadingRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class MqttIngestionEngine {

    private static final Logger log = LoggerFactory.getLogger(MqttIngestionEngine.class);
    private static final float TEMP_MAX_THRESHOLD = 35.0f;

    @Value("${MQTT_HOST:127.0.0.1}")
    private String mqttHost;

    @Value("${MQTT_PORT:1883}")
    private int mqttPort;

    private final DeviceRepository deviceRepository;
    private final ReadingRepository readingRepository;
    private final SensorWebSocketHandler webSocketHandler;
    private final DiscordWebhookService discordWebhookService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MqttClient client;

    public MqttIngestionEngine(DeviceRepository deviceRepository, 
                               ReadingRepository readingRepository, 
                               SensorWebSocketHandler webSocketHandler,
                               DiscordWebhookService discordWebhookService) {
        this.deviceRepository = deviceRepository;
        this.readingRepository = readingRepository;
        this.webSocketHandler = webSocketHandler;
        this.discordWebhookService = discordWebhookService;
    }

    @PostConstruct
    public void init() {
        Thread subscriberThread = new Thread(this::connectionLoop);
        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }

    private void connectionLoop() {
        String brokerUrl = "tcp://" + mqttHost + ":" + mqttPort;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                client = new MqttClient(brokerUrl, "SpringBootMqttEngine", new MemoryPersistence());
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                options.setAutomaticReconnect(true);

                client.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        try {
                            client.subscribe("buildingA/+/+", 1);
                            log.info("[MQTT] Successfully subscribed to topic: buildingA/+/+");
                        } catch (MqttException e) {
                            log.error("[MQTT] Telemetry subscription registration failed", e);
                            discordWebhookService.sendAlert("Broker subscription binding failure on topic pattern [buildingA/+/+]. Reason: " + e.getMessage());
                        }
                    }
                    @Override
                    public void connectionLost(Throwable cause) {
                        log.warn("[MQTT] Connection lost. Reconnecting fallback mechanism active.");
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
                log.error("[MQTT] Connection failed, retrying in 5s...", e);
                try { Thread.sleep(5000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
    }

    // Mem-parse dan menyimpan satu pesan MQTT ke Cassandra.
    // Format topik yang diharapkan: buildingA/{ruangan}/{device-uuid}
    public void processPacket(String topic, MqttMessage message) {
        try {
            String[] parts = topic.split("/");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Segment sequence length validation failed for route mappings.");
            }
            
            UUID deviceId = UUID.fromString(parts[2]);            
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), new TypeReference<>() {});

            if (!deviceRepository.existsById(deviceId)) {
                log.warn("[MQTT] Telemetry rejected. Unknown device ID: {}", deviceId);
                discordWebhookService.sendAlert(String.format("Unregistered Device Attempt!\n**Topic:** `%s`\n**Device ID:** `%s` has skipped validation mapping bounds.", topic, deviceId));
                return;
            }

            float temperature = ((Number) payload.get("temperature")).floatValue();
            float humidity = ((Number) payload.get("humidity")).floatValue();
            long tsDevice = ((Number) payload.get("ts")).longValue();

            long tsReceive = System.currentTimeMillis();
            String bucketDate = Instant.ofEpochMilli(tsReceive).toString().substring(0, 10);

            ReadingKey key = new ReadingKey(deviceId, bucketDate, tsDevice);
            Reading reading = new Reading();
            reading.setKey(key);
            reading.setTsReceive(tsReceive);
            reading.setTemperature(temperature);
            reading.setHumidity(humidity);
            
            readingRepository.save(reading);

            String normalJson = objectMapper.writeValueAsString(new WebSocketEvent<>("READING", reading));
            webSocketHandler.broadcast(normalJson);

            if (temperature > TEMP_MAX_THRESHOLD) {
                Map<String, Object> alertPayload = Map.of(
                    "deviceId", deviceId, 
                    "metric", "temperature", 
                    "value", temperature, 
                    "message", "CRITICAL OVERHEAT DETECTED!"
                );
                String alertJson = objectMapper.writeValueAsString(new WebSocketEvent<>("ALERT", alertPayload));
                webSocketHandler.broadcast(alertJson);
                log.warn("[ALERT] Device {} exceeded safety threshold: {}°C", deviceId, temperature);

                discordWebhookService.sendAlert(String.format("**CRITICAL OVERHEAT ALERT**\n**Device:** `%s`\n**Temperature:** `%.2f°C` (Safety limit: %s°C)", 
                        deviceId, temperature, TEMP_MAX_THRESHOLD));
            }
            
        } catch (Exception e) {
            log.error("[MQTT] Failed to process telemetry packet", e);
            discordWebhookService.sendAlert(String.format("**Asynchronous Ingestion Process Failure**\n**Topic:** `%s`\n**Exception Type:** `%s`\n**Error Trace message:** %s", 
                    topic, e.getClass().getSimpleName(), e.getMessage()));
        }
    }

    @PreDestroy
    public void cleanUp() {
        try { if (client != null && client.isConnected()) client.disconnect(); } catch (MqttException ignored) {}
    }
}
