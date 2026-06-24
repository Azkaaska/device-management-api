package com.iot.deviceapi.service;

import com.iot.deviceapi.handler.SensorWebSocketHandler;
import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.repository.DeviceRepository;
import com.iot.deviceapi.repository.ReadingRepository;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MqttIngestionEngineTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private ReadingRepository readingRepository;

    @Mock
    private SensorWebSocketHandler webSocketHandler;

    @Mock
    private DiscordWebhookService discordWebhookService;

    @InjectMocks
    private MqttIngestionEngine mqttIngestionEngine;

    private UUID deviceId;
    private String validTopic;

    @BeforeEach
    void setUp() {
        deviceId = UUID.randomUUID();
        validTopic = "buildingA/room1/" + deviceId;
    }

    // Membuat MqttMessage dari string JSON biasa
    private MqttMessage mqttMsg(String json) {
        MqttMessage msg = new MqttMessage();
        msg.setPayload(json.getBytes(StandardCharsets.UTF_8));
        return msg;
    }

    @Test
    void processPacket_validPayload_savesReadingAndBroadcasts() throws Exception {
        String json = """
                {"ts":1700000000000,"temperature":28.0,"humidity":65.0}
                """;

        when(deviceRepository.existsById(deviceId)).thenReturn(true);
        when(readingRepository.save(any(Reading.class))).thenAnswer(inv -> inv.getArgument(0));

        mqttIngestionEngine.processPacket(validTopic, mqttMsg(json));

        verify(readingRepository).save(any(Reading.class));
        verify(webSocketHandler).broadcast(anyString());
    }

    @Test
    void processPacket_unknownDevice_rejectsAndSendsAlert() {
        String json = """
                {"ts":1700000000000,"temperature":20.0,"humidity":50.0}
                """;

        when(deviceRepository.existsById(deviceId)).thenReturn(false);

        mqttIngestionEngine.processPacket(validTopic, mqttMsg(json));

        verify(readingRepository, never()).save(any());
        verify(discordWebhookService).sendAlert(anyString());
    }

    @Test
    void processPacket_tooFewTopicSegments_throwsAndSendsAlert() {
        // Topik dengan hanya 2 segmen tidak valid (butuh minimal 3)
        mqttIngestionEngine.processPacket("buildingA/room1", mqttMsg("{}"));

        verify(readingRepository, never()).save(any());
        verify(discordWebhookService).sendAlert(anyString());
    }

    @Test
    void processPacket_invalidDeviceIdInTopic_throwsAndSendsAlert() {
        mqttIngestionEngine.processPacket("buildingA/room1/not-a-uuid", mqttMsg(
                "{\"ts\":1700000000000,\"temperature\":20.0,\"humidity\":50.0}"));

        verify(readingRepository, never()).save(any());
        verify(discordWebhookService).sendAlert(anyString());
    }

    @Test
    void processPacket_overTemperatureThreshold_broadcastsAlertAndNotifiesDiscord() {
        // 36°C > TEMP_MAX_THRESHOLD (35°C) sehingga event ALERT juga harus ikut di-broadcast
        String json = """
                {"ts":1700000000000,"temperature":36.0,"humidity":60.0}
                """;

        when(deviceRepository.existsById(deviceId)).thenReturn(true);
        when(readingRepository.save(any(Reading.class))).thenAnswer(inv -> inv.getArgument(0));

        mqttIngestionEngine.processPacket(validTopic, mqttMsg(json));

        // broadcast dipanggil dua kali: sekali untuk READING, sekali untuk ALERT
        verify(webSocketHandler, times(2)).broadcast(anyString());
        verify(discordWebhookService).sendAlert(contains("CRITICAL OVERHEAT"));
    }

    @Test
    void processPacket_belowThreshold_noAlertBroadcast() {
        String json = """
                {"ts":1700000000000,"temperature":25.0,"humidity":60.0}
                """;

        when(deviceRepository.existsById(deviceId)).thenReturn(true);
        when(readingRepository.save(any(Reading.class))).thenAnswer(inv -> inv.getArgument(0));

        mqttIngestionEngine.processPacket(validTopic, mqttMsg(json));

        // Hanya satu broadcast untuk READING normal, tidak ada alert Discord
        verify(webSocketHandler, times(1)).broadcast(anyString());
        verifyNoInteractions(discordWebhookService);
    }

    @Test
    void processPacket_malformedJson_doesNotSaveAndSendsAlert() {
        mqttIngestionEngine.processPacket(validTopic, mqttMsg("this-is-not-json"));

        verify(readingRepository, never()).save(any());
        verify(discordWebhookService).sendAlert(anyString());
    }
}
