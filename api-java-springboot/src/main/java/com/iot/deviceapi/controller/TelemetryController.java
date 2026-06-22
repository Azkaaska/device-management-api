package com.iot.deviceapi.controller;

import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.model.ReadingInput;
import com.iot.deviceapi.service.TelemetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@Tag(name = "Telemetry", description = "Operations related to pushing and retrieving time-series data")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @GetMapping("/{id}/telemetry")
    @Operation(summary = "Get telemetry for a device", description = "Retrieves the single most recent telemetry record, or a paginated array of historical readings if time parameters match.")
    public Object getTelemetry(
            @PathVariable UUID id,
            @RequestParam(name = "start_time", required = false) Long startTime,
            @RequestParam(name = "end_time", required = false) Long endTime,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {

        if (startTime != null && endTime != null) {
            return telemetryService.getHistoricalReadings(id, startTime, endTime, page, limit);
        } else {
            Reading latest = telemetryService.getLatestReading(id);
            return latest != null ? latest : new HashMap<>();
        }
    }

    @PostMapping("/{id}/telemetry")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Push telemetry to a device")
    public Reading pushTelemetry(@PathVariable UUID id, @RequestBody ReadingInput input) {
        return telemetryService.pushTelemetry(id, input);
    }
}
