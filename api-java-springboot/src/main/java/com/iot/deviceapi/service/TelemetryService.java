package com.iot.deviceapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.deviceapi.handler.SensorWebSocketHandler;
import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.model.ReadingInput;
import com.iot.deviceapi.model.ReadingKey;
import com.iot.deviceapi.model.WebSocketEvent;
import com.iot.deviceapi.repository.ReadingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TelemetryService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryService.class);

    private final ReadingRepository readingRepository;
    private final DeviceService deviceService;
    private final SensorWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TelemetryService(ReadingRepository readingRepository, DeviceService deviceService, SensorWebSocketHandler webSocketHandler) {
        this.readingRepository = readingRepository;
        this.deviceService = deviceService;
        this.webSocketHandler = webSocketHandler;
    }

    public Reading pushTelemetry(UUID id, ReadingInput input) {
        deviceService.getDeviceById(id);

        long tsReceive = System.currentTimeMillis();
        String bucketDate = Instant.ofEpochMilli(tsReceive).toString().substring(0, 10);

        ReadingKey key = new ReadingKey(id, bucketDate, input.getTs());
        Reading reading = new Reading();
        reading.setKey(key);
        reading.setTsReceive(tsReceive);
        reading.setTemperature(input.getTemperature());
        reading.setHumidity(input.getHumidity());

        Reading savedReading = readingRepository.save(reading);

        // Broadcast WebSocket bersifat best-effort: kegagalan tidak boleh mengganggu alur penulisan data utama
        try {
            String wsPayload = objectMapper.writeValueAsString(new WebSocketEvent<>("READING", savedReading));
            webSocketHandler.broadcast(wsPayload);
        } catch (Exception e) {
            log.error("[TELEMETRY SERVICE] Serialization failure during HTTP broadcast trigger", e);
        }

        return savedReading;
    }

    public Reading getLatestReading(UUID deviceId) {
        long now = System.currentTimeMillis();

        // Cassandra mempartisi data berdasarkan bucket_date, sehingga kita memindai hingga 8 hari
        // ke belakang untuk menemukan data terbaru jika perangkat tidak aktif beberapa hari
        for (int i = 0; i < 8; i++) {
            long targetTs = now - ((long) i * 86400000L);
            String bucketDate = Instant.ofEpochMilli(targetTs).toString().substring(0, 10);
            
            List<Reading> list = readingRepository.findLatestByDeviceAndBucket(deviceId, bucketDate);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    public List<Reading> getHistoricalReadings(UUID deviceId, Long startTime, Long endTime, int page, int limit) {
        List<Reading> allReadings = new ArrayList<>();

        // Setiap partisi Cassandra mencakup satu hari kalender (bucket_date).
        // Setiap hari dalam rentang waktu harus diquery secara terpisah, lalu hasilnya digabungkan di memori.
        LocalDate start = LocalDate.parse(Instant.ofEpochMilli(startTime).toString().substring(0, 10));
        LocalDate end = LocalDate.parse(Instant.ofEpochMilli(endTime).toString().substring(0, 10));

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String bucket = date.toString();
            List<Reading> list = readingRepository.findByDeviceAndBucketAndTsRange(deviceId, bucket, startTime, endTime);
            if (list != null) {
                allReadings.addAll(list);
            }
        }

        allReadings.sort((a, b) -> b.getTsDevice().compareTo(a.getTsDevice()));

        int fromIndex = page * limit;
        if (fromIndex >= allReadings.size()) return new ArrayList<>();
        int toIndex = Math.min(fromIndex + limit, allReadings.size());

        return allReadings.subList(fromIndex, toIndex);
    }
}
