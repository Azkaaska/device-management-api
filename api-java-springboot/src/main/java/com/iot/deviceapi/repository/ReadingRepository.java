package com.iot.deviceapi.repository;

import com.iot.deviceapi.model.Reading;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

public interface ReadingRepository extends JpaRepository<Reading, Long> {
    Optional<Reading> findFirstByDeviceIdOrderByTsDesc(UUID deviceId);
    List<Reading> findAllByDeviceIdAndTsBetweenOrderByTsDesc(UUID deviceId, Long start, Long end);
}
