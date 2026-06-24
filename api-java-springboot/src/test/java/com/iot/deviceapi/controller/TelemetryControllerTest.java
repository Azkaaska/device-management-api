package com.iot.deviceapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.deviceapi.exception.GlobalExceptionHandler;
import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.model.ReadingInput;
import com.iot.deviceapi.model.ReadingKey;
import com.iot.deviceapi.service.TelemetryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TelemetryControllerTest {

    @Mock
    private TelemetryService telemetryService;

    @InjectMocks
    private TelemetryController telemetryController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private UUID deviceId;
    private Reading reading;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(telemetryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        deviceId = UUID.randomUUID();

        ReadingKey key = new ReadingKey(deviceId, LocalDate.now().toString(), 1700000000000L);
        reading = new Reading();
        reading.setKey(key);
        reading.setTemperature(28.5f);
        reading.setHumidity(70.0f);
        reading.setTsReceive(System.currentTimeMillis());
    }

    @Test
    void getTelemetry_noTimeParams_returnsLatestReading() throws Exception {
        when(telemetryService.getLatestReading(deviceId)).thenReturn(reading);

        mockMvc.perform(get("/api/v1/devices/{id}/telemetry", deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(28.5));
    }

    @Test
    void getTelemetry_noTimeParams_noReadingFound_returnsEmptyObject() throws Exception {
        when(telemetryService.getLatestReading(deviceId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/devices/{id}/telemetry", deviceId))
                .andExpect(status().isOk())
                // objek JSON kosong {} dikembalikan ketika tidak ada data yang tersedia
                .andExpect(content().json("{}"));
    }

    @Test
    void getTelemetry_withTimeParams_returnsHistoricalList() throws Exception {
        when(telemetryService.getHistoricalReadings(eq(deviceId), anyLong(), anyLong(), eq(0), eq(20)))
                .thenReturn(List.of(reading));

        mockMvc.perform(get("/api/v1/devices/{id}/telemetry", deviceId)
                        .param("start_time", "1717200000000")
                        .param("end_time", "1717286400000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].temperature").value(28.5));
    }

    @Test
    void getTelemetry_deviceNotFound_returns404() throws Exception {
        when(telemetryService.getLatestReading(deviceId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        mockMvc.perform(get("/api/v1/devices/{id}/telemetry", deviceId))
                .andExpect(status().isNotFound());
    }

    @Test
    void pushTelemetry_validInput_returns201() throws Exception {
        ReadingInput input = new ReadingInput();
        input.setTs(System.currentTimeMillis());
        input.setTemperature(28.5f);
        input.setHumidity(70.0f);

        when(telemetryService.pushTelemetry(eq(deviceId), any(ReadingInput.class))).thenReturn(reading);

        mockMvc.perform(post("/api/v1/devices/{id}/telemetry", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.temperature").value(28.5));
    }

    @Test
    void pushTelemetry_deviceNotFound_returns404() throws Exception {
        ReadingInput input = new ReadingInput();
        input.setTs(System.currentTimeMillis());
        input.setTemperature(28.5f);
        input.setHumidity(70.0f);

        when(telemetryService.pushTelemetry(eq(deviceId), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        mockMvc.perform(post("/api/v1/devices/{id}/telemetry", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void pushTelemetry_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/devices/{id}/telemetry", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest());
    }
}
