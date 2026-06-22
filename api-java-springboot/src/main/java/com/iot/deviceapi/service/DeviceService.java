package com.iot.deviceapi.service;

import com.iot.deviceapi.model.Device;
import com.iot.deviceapi.model.DeviceInput;
import com.iot.deviceapi.repository.DeviceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DiscordWebhookService discordWebhookService;

    public DeviceService(DeviceRepository deviceRepository, DiscordWebhookService discordWebhookService) {
        this.deviceRepository = deviceRepository;
        this.discordWebhookService = discordWebhookService;
    }

    @Transactional(readOnly = true)
    public List<Device> getAllDevices(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return deviceRepository.findAll(pageable).getContent();
    }

    @Transactional(readOnly = true)
    public Device getDeviceById(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return deviceRepository.existsById(id);
    }

    public Device createDevice(DeviceInput input) {
        validateInput(input);
        Device device = new Device();
        device.setDeviceName(input.getDeviceName());
        device.setDeviceType(input.getDeviceType());
        device.setStatus(input.getStatus() != null ? input.getStatus() : "ACTIVE");
        device.setFirmwareVersion(input.getFirmwareVersion());
        device.setDeviceMetadata(input.getDeviceMetadata());
        
        Device savedDevice = deviceRepository.save(device);
        discordWebhookService.sendDeviceCreatedNotification(savedDevice);

        return savedDevice;
    }

    public Device updateDevice(UUID id, DeviceInput input) {
        validateInput(input);
        Device device = getDeviceById(id);
        device.setDeviceName(input.getDeviceName());
        device.setDeviceType(input.getDeviceType());
        if (input.getStatus() != null) {
            device.setStatus(input.getStatus());
        }
        device.setFirmwareVersion(input.getFirmwareVersion());
        device.setDeviceMetadata(input.getDeviceMetadata());
        return deviceRepository.save(device);
    }

    public void softDeleteDevice(UUID id) {
        Device device = getDeviceById(id);
        device.setStatus("INACTIVE");
        deviceRepository.save(device);
    }

    private void validateInput(DeviceInput input) {
        if (input.getDeviceName() == null || input.getDeviceName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "device_name is required");
        }
    }
}
