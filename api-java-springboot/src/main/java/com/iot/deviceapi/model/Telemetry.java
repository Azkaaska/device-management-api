package com.iot.deviceapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.UUID;

@Entity
@Table(name = "telemetry")
@JsonPropertyOrder({ "id", "device_id", "temperature", "humidity", "ts" })
public class Telemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false, foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE"))
    @JsonIgnore
    private Device device;

    @Column(name = "device_id", nullable = false, insertable = false, updatable = false)
    @JsonProperty("device_id")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID deviceId;

    @Schema(example = "22.5")
    private Double temperature;

    @Schema(example = "45")
    private Double humidity;

    @Column(nullable = false)
    @Schema(example = "1780894449950")
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

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
        if (device != null) {
            this.deviceId = device.getId();
        }
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }
}
