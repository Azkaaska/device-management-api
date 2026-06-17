package com.iot.deviceapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.*;

public class MqttEmulator {

    private static final String MQTT_HOST = System.getenv().getOrDefault("MQTT_HOST", "127.0.0.1");
    private static final int MQTT_PORT = Integer.parseInt(System.getenv().getOrDefault("MQTT_PORT", "1883"));
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Topic: {place1}/{place2}/{deviceId}
    private static final List<Map<String, String>> DEVICES = Arrays.asList(
        createDevice("550e8400-e29b-41d4-a716-446655440001", "thermometer",
                     "buildingA/floor2/550e8400-e29b-41d4-a716-446655440001"),
        createDevice("550e8400-e29b-41d4-a716-446655440002", "energymeter",
                     "plantB/chiller4/550e8400-e29b-41d4-a716-446655440002"),
        createDevice("550e8400-e29b-41d4-a716-446655440003", "waterflow",
                     "plantB/coolingTower/550e8400-e29b-41d4-a716-446655440003")
    );

    public static void main(String[] args) {
        System.out.println("Starting Standalone Java MQTT Emulator...");
        String brokerUrl = "tcp://" + MQTT_HOST + ":" + MQTT_PORT;
        try {
            try (MqttClient client = new MqttClient(brokerUrl, "JavaMqttEmulator", new MemoryPersistence())) {
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                client.connect(options);
                System.out.println("Emulator connected to broker: " + brokerUrl);

                Random random = new Random();
                double energyAccumulated = random.nextDouble() * 40.0;
                double waterVolumeAccumulated = random.nextDouble() * 400.0;

                while (true) {
                    long ts = System.currentTimeMillis();

                    for (Map<String, String> device : DEVICES) {
                        String deviceType = device.get("deviceType");
                        String topic = device.get("topic");

                        Map<String, Object> sensorValues = new HashMap<>();

                        if ("thermometer".equals(deviceType)) {
                            sensorValues.put("temperature", Math.round((random.nextDouble() * 15 + 20) * 100.0) / 100.0);
                            sensorValues.put("humidity",    Math.round((random.nextDouble() * 40 + 40) * 100.0) / 100.0);
                        } else if ("energymeter".equals(deviceType)) {
                            double voltage     = Math.round((random.nextDouble() * 20 + 220) * 10.0) / 10.0;
                            double current     = Math.round((random.nextDouble() * 7.5 + 0.5) * 100.0) / 100.0;
                            double activePower = Math.round(((voltage * current * 0.9) / 1000.0) * 1000.0) / 1000.0;
                            energyAccumulated += activePower * (2.0 / 3600.0);
                            sensorValues.put("voltage",      voltage);
                            sensorValues.put("current",      current);
                            sensorValues.put("active_power", activePower);
                            sensorValues.put("total_energy", Math.round(energyAccumulated * 10000.0) / 10000.0);
                        } else if ("waterflow".equals(deviceType)) {
                            double flowRate = Math.round((random.nextDouble() * 20 + 5) * 100.0) / 100.0;
                            waterVolumeAccumulated += flowRate * (2.0 / 60.0);
                            sensorValues.put("flow_rate",    flowRate);
                            sensorValues.put("total_volume", Math.round(waterVolumeAccumulated * 100.0) / 100.0);
                        }

                        Map<String, Object> payload = new LinkedHashMap<>();
                        payload.put("ts", ts);
                        payload.put("sensor_values", sensorValues);

                        String jsonPayload = objectMapper.writeValueAsString(payload);
                        MqttMessage message = new MqttMessage(jsonPayload.getBytes());
                        message.setQos(1);
                        client.publish(topic, message);
                        System.out.println("[" + deviceType + "] → " + topic + " | " + jsonPayload);
                    }

                    Thread.sleep(2000);
                }
            }

        } catch (Exception e) {
            System.err.println("Emulator execution failed: " + e.getMessage());
        }
    }

    private static Map<String, String> createDevice(String id, String type, String topic) {
        Map<String, String> dev = new HashMap<>();
        dev.put("deviceId", id);
        dev.put("deviceType", type);
        dev.put("topic", topic);
        return dev;
    }
}
