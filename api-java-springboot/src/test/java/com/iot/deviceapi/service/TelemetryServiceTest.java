package com.iot.deviceapi.service;

import com.iot.deviceapi.handler.SensorWebSocketHandler;
import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.model.ReadingInput;
import com.iot.deviceapi.model.ReadingKey;
import com.iot.deviceapi.repository.ReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryServiceTest {

    @Mock
    private ReadingRepository readingRepository;

    @Mock
    private DeviceService deviceService;

    @Mock
    private SensorWebSocketHandler webSocketHandler;

    @InjectMocks
    private TelemetryService telemetryService;

    private UUID deviceId;
    private Reading reading;

    @BeforeEach
    void setUp() {
        deviceId = UUID.randomUUID();
        String today = LocalDate.now().toString();

        ReadingKey key = new ReadingKey(deviceId, today, System.currentTimeMillis());
        reading = new Reading();
        reading.setKey(key);
        reading.setTemperature(25.0f);
        reading.setHumidity(60.0f);
        reading.setTsReceive(System.currentTimeMillis());
    }

    @Test
    void pushTelemetry_savesReadingAndBroadcasts() {
        ReadingInput input = new ReadingInput();
        input.setTs(System.currentTimeMillis());
        input.setTemperature(28.5f);
        input.setHumidity(70.0f);

        when(readingRepository.save(any(Reading.class))).thenReturn(reading);

        Reading result = telemetryService.pushTelemetry(deviceId, input);

        assertThat(result).isNotNull();
        verify(readingRepository).save(any(Reading.class));
        // broadcast bersifat best-effort, verifikasi bahwa metode dipanggil tepat sekali
        verify(webSocketHandler).broadcast(anyString());
    }

    @Test
    void pushTelemetry_deviceNotFound_throws404() {
        ReadingInput input = new ReadingInput();
        input.setTs(System.currentTimeMillis());
        input.setTemperature(20.0f);
        input.setHumidity(50.0f);

        doThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Device not found"))
                .when(deviceService).getDeviceById(deviceId);

        assertThatThrownBy(() -> telemetryService.pushTelemetry(deviceId, input))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Device not found");

        verify(readingRepository, never()).save(any());
    }

    @Test
    void getLatestReading_foundInCurrentBucket_returnsReading() {
        String today = LocalDate.now().toString();
        when(readingRepository.findLatestByDeviceAndBucket(deviceId, today))
                .thenReturn(List.of(reading));

        Reading result = telemetryService.getLatestReading(deviceId);

        assertThat(result).isNotNull();
        assertThat(result.getTemperature()).isEqualTo(25.0f);
    }

    @Test
    void getLatestReading_noReadingsInAnyBucket_returnsNull() {
        // Semua 8 pencarian bucket mengembalikan daftar kosong
        when(readingRepository.findLatestByDeviceAndBucket(any(), anyString()))
                .thenReturn(List.of());

        Reading result = telemetryService.getLatestReading(deviceId);

        assertThat(result).isNull();
    }

    @Test
    void getLatestReading_firstBucketEmpty_secondBucketHasData() {
        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();

        when(readingRepository.findLatestByDeviceAndBucket(deviceId, today))
                .thenReturn(List.of());
        when(readingRepository.findLatestByDeviceAndBucket(deviceId, yesterday))
                .thenReturn(List.of(reading));

        Reading result = telemetryService.getLatestReading(deviceId);

        assertThat(result).isNotNull();
    }

    @Test
    void getHistoricalReadings_singleDayRange_returnsMergedAndPaged() {
        long start = Instant.parse("2024-06-01T00:00:00Z").toEpochMilli();
        long end   = Instant.parse("2024-06-01T23:59:59Z").toEpochMilli();

        when(readingRepository.findByDeviceAndBucketAndTsRange(deviceId, "2024-06-01", start, end))
                .thenReturn(List.of(reading));

        List<Reading> result = telemetryService.getHistoricalReadings(deviceId, start, end, 0, 20);

        assertThat(result).hasSize(1);
    }

    @Test
    void getHistoricalReadings_multiDayRange_queriesEachBucket() {
        long start = Instant.parse("2024-06-01T00:00:00Z").toEpochMilli();
        long end   = Instant.parse("2024-06-03T23:59:59Z").toEpochMilli();

        when(readingRepository.findByDeviceAndBucketAndTsRange(eq(deviceId), anyString(), anyLong(), anyLong()))
                .thenReturn(List.of(reading));

        List<Reading> result = telemetryService.getHistoricalReadings(deviceId, start, end, 0, 20);

        // 3 hari diquery → 3 data digabungkan
        assertThat(result).hasSize(3);
        // repository harus dipanggil tepat sekali untuk setiap hari
        verify(readingRepository, times(3))
                .findByDeviceAndBucketAndTsRange(eq(deviceId), anyString(), anyLong(), anyLong());
    }

    @Test
    void getHistoricalReadings_pageOutOfBounds_returnsEmptyList() {
        long start = Instant.parse("2024-06-01T00:00:00Z").toEpochMilli();
        long end   = Instant.parse("2024-06-01T23:59:59Z").toEpochMilli();

        when(readingRepository.findByDeviceAndBucketAndTsRange(any(), anyString(), anyLong(), anyLong()))
                .thenReturn(List.of(reading));

        // page=99 jauh melampaui satu-satunya hasil yang ada
        List<Reading> result = telemetryService.getHistoricalReadings(deviceId, start, end, 99, 20);

        assertThat(result).isEmpty();
    }
}
