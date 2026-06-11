package com.iot.deviceapi.model;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "devices")
@Schema(description = "IoT Device metadata")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "device_id")
    @JsonProperty("device_id")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID deviceId;

    @Column(name = "device_name", nullable = false, length = 100)
    @JsonProperty("device_name")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Sensor Suhu Ruang Server")
    private String deviceName;

    @Column(name = "device_type", nullable = false, length = 50)
    @JsonProperty("device_type")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Thermometer")
    private String deviceType;

    @Column(nullable = false, length = 20)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "ACTIVE", example = "ACTIVE")
    private String status = "ACTIVE";

    @Column(name = "firmware_version", length = 20)
    @JsonProperty("firmware_version")
    @Schema(example = "v2.1.0")
    private String firmwareVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "device_metadata", columnDefinition = "jsonb")
    @JsonProperty("device_metadata")
    @Schema(example = "{\"floor\": 3, \"room\": \"301\"}")
    private Map<String, Object> deviceMetadata;

    @Column(name = "created_at", nullable = false)
    @JsonProperty("created_at")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1780894449946")
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    @JsonProperty("updated_at")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1780894449946")
    private Long updatedAt;

    @PrePersist
    protected void onCreate() {
        long now = System.currentTimeMillis();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Map<String, Object> getDeviceMetadata() {
        return deviceMetadata;
    }

    public void setDeviceMetadata(Map<String, Object> deviceMetadata) {
        this.deviceMetadata = deviceMetadata;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
