package com.iot.deviceapi.controller;

import com.iot.deviceapi.model.Device;
import com.iot.deviceapi.model.DeviceInput;
import com.iot.deviceapi.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@Tag(name = "Devices", description = "Operations related to managing device attributes")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    @Operation(summary = "Retrieve a paginated list of devices")
    public List<Device> getAllDevices(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        return deviceService.getAllDevices(page, limit);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new device")
    public Device createDevice(@RequestBody DeviceInput input) {
        return deviceService.createDevice(input);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a device by ID")
    public Device getDevice(@PathVariable UUID id) {
        return deviceService.getDeviceById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a device")
    public Device updateDevice(@PathVariable UUID id, @RequestBody DeviceInput input) {
        return deviceService.updateDevice(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a device (Soft Delete)")
    public void deleteDevice(@PathVariable UUID id) {
        deviceService.softDeleteDevice(id);
    }
}
