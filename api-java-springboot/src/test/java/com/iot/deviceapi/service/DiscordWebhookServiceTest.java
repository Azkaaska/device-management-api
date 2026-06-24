package com.iot.deviceapi.service;

import com.iot.deviceapi.model.Device;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscordWebhookServiceTest {

    @Mock
    private RestTemplate restTemplate;

    // DiscordWebhookService membuat RestTemplate secara internal, sehingga kita inject via reflection
    @InjectMocks
    private DiscordWebhookService discordWebhookService;

    private Device device;

    @BeforeEach
    void setUp() {
        device = new Device();
        device.setId(UUID.randomUUID());
        device.setName("Test Sensor");
        device.setType("Thermometer");
        device.setStatus("active");

        // Inject mock RestTemplate dan URL webhook yang tidak kosong via reflection
        ReflectionTestUtils.setField(discordWebhookService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(discordWebhookService, "discordWebhookUrl", "https://discord.com/api/webhooks/test");
    }

    @Test
    void sendDeviceCreatedNotification_postsEmbedToWebhook() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));

        discordWebhookService.sendDeviceCreatedNotification(device);

        verify(restTemplate).postForEntity(
                eq("https://discord.com/api/webhooks/test"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void sendDeviceCreatedNotification_blankUrl_doesNotCallRestTemplate() {
        ReflectionTestUtils.setField(discordWebhookService, "discordWebhookUrl", "");

        discordWebhookService.sendDeviceCreatedNotification(device);

        verifyNoInteractions(restTemplate);
    }

    @Test
    void sendDeviceCreatedNotification_networkFailure_doesNotPropagate() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenThrow(new RestClientException("Connection refused"));

        // Harus menelan exception tanpa menyebabkan crash pada pemanggil
        assertThatCode(() -> discordWebhookService.sendDeviceCreatedNotification(device))
                .doesNotThrowAnyException();
    }

    @Test
    void sendAlert_postsMessageToWebhook() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));

        discordWebhookService.sendAlert("CRITICAL OVERHEAT");

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(String.class));
        assertThat(captor.getValue().getBody().toString()).contains("CRITICAL OVERHEAT");
    }

    @Test
    void sendAlert_blankUrl_doesNotCallRestTemplate() {
        ReflectionTestUtils.setField(discordWebhookService, "discordWebhookUrl", "  ");

        discordWebhookService.sendAlert("some alert");

        verifyNoInteractions(restTemplate);
    }

    @Test
    void sendAlert_networkFailure_doesNotPropagate() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenThrow(new RestClientException("Timeout"));

        assertThatCode(() -> discordWebhookService.sendAlert("test"))
                .doesNotThrowAnyException();
    }
}
