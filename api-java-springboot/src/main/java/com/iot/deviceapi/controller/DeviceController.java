package com.iot.deviceapi.controller;

import com.iot.deviceapi.model.Device;
import com.iot.deviceapi.model.DeviceInput;
import com.iot.deviceapi.service.DeviceService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class DeviceController implements DeviceControllerDocs {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public List<Device> getAllDevices(int page, int limit) {
        return deviceService.getAllDevices(page, limit);
    }

    @Override
    public Device createDevice(DeviceInput input) {
        return deviceService.createDevice(input);
    }

    @Override
    public Device getDevice(UUID id) {
        return deviceService.getDeviceById(id);
    }

    @Override
    public Device updateDevice(UUID id, DeviceInput input) {
        return deviceService.updateDevice(id, input);
    }

    @Override
    public void deleteDevice(UUID id) {
        deviceService.softDeleteDevice(id);
    }
}
