package com.iot.deviceapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.deviceapi.exception.GlobalExceptionHandler;
import com.iot.deviceapi.model.Device;
import com.iot.deviceapi.model.DeviceInput;
import com.iot.deviceapi.service.DeviceService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DeviceControllerTest {

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private DeviceController deviceController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Device device;
    private UUID deviceId;

    @BeforeEach
    void setUp() {
        // Standalone setup menghindari kebutuhan Spring context penuh
        mockMvc = MockMvcBuilders.standaloneSetup(deviceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        deviceId = UUID.randomUUID();
        device = new Device();
        device.setId(deviceId);
        device.setName("Sensor A");
        device.setType("Thermometer");
        device.setStatus("active");
    }

    @Test
    void getAllDevices_returns200WithList() throws Exception {
        when(deviceService.getAllDevices(0, 20)).thenReturn(List.of(device));

        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sensor A"));
    }

    @Test
    void createDevice_validInput_returns201() throws Exception {
        DeviceInput input = new DeviceInput();
        input.setName("Sensor B");
        input.setType("Hygrometer");

        when(deviceService.createDevice(any(DeviceInput.class))).thenReturn(device);

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sensor A"));
    }

    @Test
    void createDevice_missingName_returns400() throws Exception {
        DeviceInput input = new DeviceInput();
        input.setType("Hygrometer");

        when(deviceService.createDevice(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required"));

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name is required"));
    }

    @Test
    void getDevice_found_returns200() throws Exception {
        when(deviceService.getDeviceById(deviceId)).thenReturn(device);

        mockMvc.perform(get("/api/v1/devices/{id}", deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(deviceId.toString()));
    }

    @Test
    void getDevice_notFound_returns404() throws Exception {
        UUID otherId = UUID.randomUUID();
        when(deviceService.getDeviceById(otherId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        mockMvc.perform(get("/api/v1/devices/{id}", otherId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getDevice_malformedUuid_returns400() throws Exception {
        // Path variable yang bukan UUID memicu MethodArgumentTypeMismatchException
        mockMvc.perform(get("/api/v1/devices/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDevice_validInput_returns200() throws Exception {
        DeviceInput input = new DeviceInput();
        input.setName("Updated");
        input.setType("Barometer");

        when(deviceService.updateDevice(eq(deviceId), any(DeviceInput.class))).thenReturn(device);

        mockMvc.perform(put("/api/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteDevice_returns204() throws Exception {
        doNothing().when(deviceService).softDeleteDevice(deviceId);

        mockMvc.perform(delete("/api/v1/devices/{id}", deviceId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDevice_notFound_returns404() throws Exception {
        UUID otherId = UUID.randomUUID();
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"))
                .when(deviceService).softDeleteDevice(otherId);

        mockMvc.perform(delete("/api/v1/devices/{id}", otherId))
                .andExpect(status().isNotFound());
    }
}
