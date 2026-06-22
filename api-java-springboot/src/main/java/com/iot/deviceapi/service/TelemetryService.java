package com.iot.deviceapi.service;

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

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ZoneId localZone = ZoneId.of("GMT+7");

    public TelemetryService(ReadingRepository readingRepository, DeviceService deviceService) {
        this.readingRepository = readingRepository;
        this.deviceService = deviceService;
    }

    public Reading pushTelemetry(UUID id, ReadingInput input) {
        deviceService.getDeviceById(id);

        long ts = System.currentTimeMillis();
        String bucketDate = Instant.ofEpochMilli(ts).atZone(localZone).format(formatter);

        ReadingKey key = new ReadingKey(id, bucketDate, ts);
        Reading reading = new Reading();
        reading.setKey(key);
        reading.setSensorValues(input.getSensorValues());

        return readingRepository.save(reading);
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

        // 1. Gather all data across the calculated date buckets
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String bucket = date.format(formatter);
            List<Reading> list = readingRepository.findByDeviceAndBucketAndTsRange(deviceId, bucket, startTime, endTime);
            if (list != null) {
                allReadings.addAll(list);
            }
        }

        // 2. Sort total combined results descending chronologically
        allReadings.sort((a, b) -> b.getTs().compareTo(a.getTs()));

        // 3. Compute target sublist slice indices based on request params
        int fromIndex = page * limit;
        if (fromIndex >= allReadings.size()) {
            return new ArrayList<>(); // Return empty array if page index drifts completely out of bounds
        }
        int toIndex = Math.min(fromIndex + limit, allReadings.size());

        return allReadings.subList(fromIndex, toIndex);
    }
}
