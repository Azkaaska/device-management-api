package com.iot.deviceapi.service;

import com.iot.deviceapi.model.Device;
import com.iot.deviceapi.model.DeviceInput;
import com.iot.deviceapi.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DiscordWebhookService discordWebhookService;

    @InjectMocks
    private DeviceService deviceService;

    private Device device;
    private UUID deviceId;

    @BeforeEach
    void setUp() {
        deviceId = UUID.randomUUID();
        device = new Device();
        device.setId(deviceId);
        device.setName("Sensor A");
        device.setType("Thermometer");
        device.setStatus("active");
    }

    @Test
    void getAllDevices_returnsPagedContent() {
        when(deviceRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(device)));

        List<Device> result = deviceService.getAllDevices(0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Sensor A");
    }

    @Test
    void getDeviceById_found_returnsDevice() {
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        Device result = deviceService.getDeviceById(deviceId);

        assertThat(result.getId()).isEqualTo(deviceId);
    }

    @Test
    void getDeviceById_notFound_throws404() {
        when(deviceRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.getDeviceById(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Device not found");
    }

    @Test
    void existsById_delegatesToRepository() {
        when(deviceRepository.existsById(deviceId)).thenReturn(true);

        assertThat(deviceService.existsById(deviceId)).isTrue();
    }

    @Test
    void createDevice_savesAndNotifiesDiscord() {
        DeviceInput input = new DeviceInput();
        input.setName("Sensor B");
        input.setType("Hygrometer");

        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        Device result = deviceService.createDevice(input);

        assertThat(result).isNotNull();
        verify(discordWebhookService).sendDeviceCreatedNotification(device);
    }

    @Test
    void createDevice_defaultsStatusToActive_whenInputStatusIsNull() {
        DeviceInput input = new DeviceInput();
        input.setName("Sensor C");
        input.setType("Hygrometer");
        input.setStatus(null);

        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

        Device result = deviceService.createDevice(input);

        assertThat(result.getStatus()).isEqualTo("active");
    }

    @Test
    void createDevice_missingName_throws400() {
        DeviceInput input = new DeviceInput();
        input.setType("Thermometer");

        assertThatThrownBy(() -> deviceService.createDevice(input))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("name is required");
    }

    @Test
    void createDevice_missingType_throws400() {
        DeviceInput input = new DeviceInput();
        input.setName("Sensor D");

        assertThatThrownBy(() -> deviceService.createDevice(input))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("type is required");
    }

    @Test
    void updateDevice_updatesFieldsAndSaves() {
        DeviceInput input = new DeviceInput();
        input.setName("Sensor Updated");
        input.setType("Barometer");
        input.setStatus("inactive");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

        Device result = deviceService.updateDevice(deviceId, input);

        assertThat(result.getName()).isEqualTo("Sensor Updated");
        assertThat(result.getType()).isEqualTo("Barometer");
        assertThat(result.getStatus()).isEqualTo("inactive");
    }

    @Test
    void updateDevice_nullStatus_doesNotOverrideExistingStatus() {
        DeviceInput input = new DeviceInput();
        input.setName("Sensor E");
        input.setType("Thermometer");
        input.setStatus(null);

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

        Device result = deviceService.updateDevice(deviceId, input);

        // status "active" yang sudah ada harus dipertahankan
        assertThat(result.getStatus()).isEqualTo("active");
    }

    @Test
    void softDeleteDevice_setsStatusToInactive() {
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

        deviceService.softDeleteDevice(deviceId);

        assertThat(device.getStatus()).isEqualTo("inactive");
        verify(deviceRepository).save(device);
    }

    @Test
    void softDeleteDevice_notFound_throws404() {
        when(deviceRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.softDeleteDevice(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Device not found");
    }
}
