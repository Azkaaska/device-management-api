package com.iot.deviceapi.controller;

import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.model.ReadingInput;
import com.iot.deviceapi.service.TelemetryService;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.UUID;

@RestController
public class TelemetryController implements TelemetryControllerDocs {

    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @Override
    public Object getTelemetry(UUID id, Long startTime, Long endTime, int page, int limit) {
        if (startTime != null && endTime != null) {
            return telemetryService.getHistoricalReadings(id, startTime, endTime, page, limit);
        } else {
            Reading latest = telemetryService.getLatestReading(id);
            return latest != null ? latest : new HashMap<>();
        }
    }

    @Override
    public Reading pushTelemetry(UUID id, ReadingInput input) {
        return telemetryService.pushTelemetry(id, input);
    }
}
