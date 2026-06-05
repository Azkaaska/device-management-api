package com.iot.deviceapi.repository;

import com.iot.deviceapi.model.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {
    Optional<Telemetry> findFirstByDeviceIdOrderByTsDesc(UUID deviceId);
}
