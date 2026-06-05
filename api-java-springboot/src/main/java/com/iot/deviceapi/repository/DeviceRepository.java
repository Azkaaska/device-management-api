package com.iot.deviceapi.repository;

import com.iot.deviceapi.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
}
