package com.iot.deviceapi.controller;

import com.iot.deviceapi.model.Device;
import com.iot.deviceapi.repository.DeviceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Devices", description = "Operations related to managing device attributes")
public class DeviceController {

    @Autowired
    private DeviceRepository deviceRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new device", description = "Registers a new IoT device in the system")
    public Device createDevice(@RequestBody Device input) {
        if (input.getName() == null || input.getName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        if (input.getStatus() == null) {
            input.setStatus("ACTIVE");
        }
        return deviceRepository.save(input);
    }

    @GetMapping
    @Operation(summary = "Retrieve a list of devices", description = "Returns an array of all registered devices")
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a device by ID", description = "Returns detailed information about a specific device")
    public Device getDevice(@PathVariable UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a device", description = "Updates the attributes of an existing device")
    public Device updateDevice(@PathVariable UUID id, @RequestBody Device input) {
        if (input.getName() == null || input.getName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        device.setName(input.getName());
        if (input.getType() != null) device.setType(input.getType());
        if (input.getStatus() != null) device.setStatus(input.getStatus());

        return deviceRepository.save(device);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a device", description = "Permanently removes a device from the system")
    public void deleteDevice(@PathVariable UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
        deviceRepository.delete(device);
    }
}
