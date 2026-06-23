package com.iot.deviceapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.deviceapi.handler.SensorWebSocketHandler;
import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.model.ReadingInput;
import com.iot.deviceapi.model.ReadingKey;
import com.iot.deviceapi.repository.ReadingRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TelemetryService {

    private final ReadingRepository readingRepository;
    private final DeviceService deviceService;
    private final SensorWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ZoneId localZone = ZoneId.of("GMT+7");

    public TelemetryService(ReadingRepository readingRepository, DeviceService deviceService, SensorWebSocketHandler webSocketHandler) {
        this.readingRepository = readingRepository;
        this.deviceService = deviceService;
        this.webSocketHandler = webSocketHandler;
    }

    public Reading pushTelemetry(UUID id, ReadingInput input) {
        deviceService.getDeviceById(id);

        long tsReceive = System.currentTimeMillis();
        long tsDevice = input.getTs();
        String bucketDate = Instant.ofEpochMilli(tsReceive).atZone(localZone).format(formatter);

        ReadingKey key = new ReadingKey(id, bucketDate, tsDevice);
        Reading reading = new Reading();
        reading.setKey(key);
        reading.setTsReceive(tsReceive);
        reading.setTemperature(input.getTemperature());
        reading.setHumidity(input.getHumidity());

        Reading savedReading = readingRepository.save(reading);

        // Also stream HTTP API pushed telemetry to the live web interface
        try {
            String wsPayload = objectMapper.writeValueAsString(savedReading);
            webSocketHandler.broadcast(id, wsPayload);
        } catch (Exception e) {
            System.err.println("[TELEMETRY SERVICE] WS Broadcast Error: " + e.getMessage());
        }

        return savedReading;
    }

    public Reading getLatestReading(UUID deviceId) {
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

    public List<Reading> getHistoricalReadings(UUID deviceId, Long startTime, Long endTime, int page, int limit) {
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

        allReadings.sort((a, b) -> b.getTsDevice().compareTo(a.getTsDevice()));

        int fromIndex = page * limit;
        if (fromIndex >= allReadings.size()) {
            return new ArrayList<>();
        }
        int toIndex = Math.min(fromIndex + limit, allReadings.size());

        return allReadings.subList(fromIndex, toIndex);
    }
}
