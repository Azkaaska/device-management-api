package com.iot.deviceapi.model;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "readings")
@Schema(description = "Device time-series telemetry data")
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Column(name = "device_id", nullable = false)
    @JsonProperty("device_id")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID deviceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sensor_values", columnDefinition = "jsonb", nullable = false)
    @JsonProperty("sensor_values")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "{\"temperature\": 24.5, \"humidity\": 60}")
    private Map<String, Object> sensorValues;

    @Column(nullable = false)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1780894449950")
    private Long ts;

    @PrePersist
    protected void onCreate() {
        if (this.ts == null) {
            this.ts = System.currentTimeMillis();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public Map<String, Object> getSensorValues() {
        return sensorValues;
    }

    public void setSensorValues(Map<String, Object> sensorValues) {
        this.sensorValues = sensorValues;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }
}
