package com.iot.deviceapi.controller;

import com.iot.deviceapi.model.Device;
import com.iot.deviceapi.model.Telemetry;
import com.iot.deviceapi.model.TelemetryInput;
import com.iot.deviceapi.repository.DeviceRepository;
import com.iot.deviceapi.repository.TelemetryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Telemetry", description = "Operations related to pushing and retrieving time-series data")
public class TelemetryController {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private TelemetryRepository telemetryRepository;

    @GetMapping("/{id}/telemetry")
    @Operation(summary = "Get latest telemetry for a device", description = "Retrieves the single most recent telemetry record")
    public Object getLatestTelemetry(@PathVariable UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        return telemetryRepository.findFirstByDeviceIdOrderByTsDesc(device.getId())
                .map(t -> (Object) t)
                .orElseGet(HashMap::new);
    }
    
    @PostMapping("/{id}/telemetry")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Push telemetry to a device", description = "Records a new telemetry data point for a specific device")
    public Telemetry pushTelemetry(@PathVariable UUID id, @RequestBody TelemetryInput input) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        Telemetry telemetry = new Telemetry();
        telemetry.setDevice(device);
        telemetry.setTemperature(input.getTemperature());
        telemetry.setHumidity(input.getHumidity());
        if (telemetry.getTs() == null) {
            telemetry.setTs(System.currentTimeMillis());
        }
        return telemetryRepository.save(telemetry);
    }
}
