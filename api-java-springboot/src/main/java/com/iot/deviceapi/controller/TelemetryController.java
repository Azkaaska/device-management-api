package com.iot.deviceapi.controller;

import com.iot.deviceapi.model.Device;
import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.model.ReadingInput;
import com.iot.deviceapi.repository.DeviceRepository;
import com.iot.deviceapi.repository.ReadingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
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
    private ReadingRepository readingRepository;

    @GetMapping("/{id}/telemetry")
    @Transactional(readOnly = true)
    @Operation(summary = "Get telemetry for a device", description = "Retrieves the single most recent telemetry record, or a list of historical readings if start_time and end_time are provided.")
    public Object getTelemetry(
            @PathVariable UUID id,
            @RequestParam(name = "start_time", required = false) Long startTime,
            @RequestParam(name = "end_time", required = false) Long endTime) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
        if (startTime != null && endTime != null) {
            return readingRepository.findAllByDeviceIdAndTsBetweenOrderByTsDesc(device.getDeviceId(), startTime, endTime);
        } else {
            return readingRepository.findFirstByDeviceIdOrderByTsDesc(device.getDeviceId())
                    .map(t -> (Object) t)
                    .orElseGet(HashMap::new);
        }
    }

    @PostMapping("/{id}/telemetry")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @Operation(summary = "Push telemetry to a device", description = "Records a new telemetry data point for a specific device")
    public Reading pushTelemetry(@PathVariable UUID id, @RequestBody ReadingInput input) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
        Reading reading = new Reading();
        reading.setDeviceId(device.getDeviceId());
        reading.setSensorValues(input.getSensorValues());
        if (reading.getTs() == null) {
            reading.setTs(System.currentTimeMillis());
        }
        return readingRepository.save(reading);
    }
}
