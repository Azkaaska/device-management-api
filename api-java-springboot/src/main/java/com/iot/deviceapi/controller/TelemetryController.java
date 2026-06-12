package com.iot.deviceapi.controller;

import com.iot.deviceapi.model.Device;
import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.model.ReadingInput;
import com.iot.deviceapi.model.ReadingKey;
import com.iot.deviceapi.repository.DeviceRepository;
import com.iot.deviceapi.repository.ReadingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Telemetry", description = "Operations related to pushing and retrieving time-series data")
public class TelemetryController {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ReadingRepository readingRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ZoneId localZone = ZoneId.of("GMT+7");

    @GetMapping("/{id}/telemetry")
    @Operation(summary = "Get telemetry for a device", description = "Retrieves the single most recent telemetry record, or a list of historical readings if start_time and end_time are provided.")
    public Object getTelemetry(
            @PathVariable UUID id,
            @RequestParam(name = "start_time", required = false) Long startTime,
            @RequestParam(name = "end_time", required = false) Long endTime) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        if (startTime != null && endTime != null) {
            return getHistoricalReadings(device.getDeviceId(), startTime, endTime);
        } else {
            Reading latest = getLatestReading(device.getDeviceId());
            return latest != null ? latest : new HashMap<>();
        }
    }

    @PostMapping("/{id}/telemetry")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Push telemetry to a device", description = "Records a new telemetry data point for a specific device")
    public Reading pushTelemetry(@PathVariable UUID id, @RequestBody ReadingInput input) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        long ts = System.currentTimeMillis();
        String bucketDate = Instant.ofEpochMilli(ts).atZone(localZone).format(formatter);

        ReadingKey key = new ReadingKey(device.getDeviceId(), bucketDate, ts);
        Reading reading = new Reading();
        reading.setKey(key);
        reading.setSensorValues(input.getSensorValues());

        return readingRepository.save(reading);
    }

    private Reading getLatestReading(UUID deviceId) {
        long now = System.currentTimeMillis();
        ZonedDateTime nowLocal = Instant.ofEpochMilli(now).atZone(localZone);
        for (int i = 0; i < 8; i++) {
            String bucketDate = nowLocal.minusDays(i).format(formatter);
            List<Reading> list = readingRepository.findLatestByDeviceAndBucket(deviceId, bucketDate);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    private List<Reading> getHistoricalReadings(UUID deviceId, Long startTime, Long endTime) {
        ZonedDateTime startLocal = Instant.ofEpochMilli(startTime).atZone(localZone);
        ZonedDateTime endLocal = Instant.ofEpochMilli(endTime).atZone(localZone);

        List<Reading> allReadings = new ArrayList<>();
        LocalDate start = startLocal.toLocalDate();
        LocalDate end = endLocal.toLocalDate();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String bucket = date.format(formatter);
            List<Reading> list = readingRepository.findByDeviceAndBucketAndTsRange(deviceId, bucket, startTime, endTime);
            if (list != null) {
                allReadings.addAll(list);
            }
        }

        allReadings.sort((a, b) -> b.getTs().compareTo(a.getTs()));
        return allReadings;
    }
}
