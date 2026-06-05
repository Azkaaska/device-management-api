package com.iot.deviceapi.controller;

import com.iot.deviceapi.model.Device;
import com.iot.deviceapi.model.Telemetry;
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

    public static class TelemetryResponse {
        public Long id;
        public UUID deviceId;
        public Double temperature;
        public Double humidity;
        public Long ts;

        public TelemetryResponse(Telemetry t) {
            this.id = t.getId();
            this.deviceId = t.getDevice().getId();
            this.temperature = t.getTemperature();
            this.humidity = t.getHumidity();
            this.ts = t.getTs();
        }
    }

    @PostMapping("/{id}/telemetry")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Push telemetry to a device", description = "Records a new telemetry data point for a specific device")
    public TelemetryResponse pushTelemetry(@PathVariable UUID id, @RequestBody Telemetry input) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        input.setDevice(device);
        if (input.getTs() == null) {
            input.setTs(System.currentTimeMillis());
        }
        Telemetry saved = telemetryRepository.save(input);
        return new TelemetryResponse(saved);
    }

    @GetMapping("/{id}/telemetry")
    @Operation(summary = "Get latest telemetry for a device", description = "Retrieves the single most recent telemetry record")
    public Object getLatestTelemetry(@PathVariable UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        return telemetryRepository.findFirstByDeviceIdOrderByTsDesc(device.getId())
                .map(TelemetryResponse::new)
                .map(t -> (Object) t)
                .orElseGet(HashMap::new);
    }
}
