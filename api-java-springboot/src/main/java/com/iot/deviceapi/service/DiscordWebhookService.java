package com.iot.deviceapi.service;

import com.iot.deviceapi.model.Device;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class DiscordWebhookService {

    private final RestTemplate restTemplate;

    @Value("${discord.webhook.url:}")
    private String discordWebhookUrl;

    public DiscordWebhookService() {
        this.restTemplate = new RestTemplate();
    }

    public void sendDeviceCreatedNotification(Device device) {
        if (discordWebhookUrl == null || discordWebhookUrl.isBlank()) {
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> embed = Map.of(
                    "title", "New IoT Device Registered",
                    "color", 3066993,
                    "fields", List.of(
                            Map.of("name", "Device ID", "value", device.getId().toString(), "inline", false),
                            Map.of("name", "Name", "value", device.getName(), "inline", true),
                            Map.of("name", "Type", "value", device.getType(), "inline", true),
                            Map.of("name", "Initial Status", "value", device.getStatus(), "inline", true)
                    ),
                    "footer", Map.of("text", "Shoutout to Java Backend")

            );

            Map<String, Object> payload = Map.of(
                    "embeds", List.of(embed)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(discordWebhookUrl, request, String.class);

        } catch (Exception e) {
            System.err.println("Discord Webhook Warning: Connection failed. Message: " + e.getMessage());
        }
    }

    public void sendAlert(String message) {
        // if (discordWebhookUrl == null || discordWebhookUrl.isBlank()) {
        // }

        // try {
        //     HttpHeaders headers = new HttpHeaders();
        //     headers.setContentType(MediaType.APPLICATION_JSON);

        //     Map<String, Object> embed = Map.of(
        //             "title", "IoT System Exception Alert",
        //             "description", message,
        //             "color", 15158332
        //     );

        //     Map<String, Object> payload = Map.of(
        //             "embeds", List.of(embed)
        //     );

        //     HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        //     restTemplate.postForEntity(discordWebhookUrl, request, String.class);

        // } catch (Exception e) {
        //     System.err.println("Discord Critical Alert Drop: Webhook failed to dispatch. Message: " + e.getMessage());
        // }
    }
}
