package com.iot.deviceapi.model;

import jakarta.persistence.*;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Column(nullable = false)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Living Room Sensor")
    private String name;

    @Schema(example = "TEMP_HUMIDITY")
    private String type;

    @Schema(defaultValue = "ACTIVE", example = "ACTIVE")
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    @JsonProperty("created_at")
    @Schema(example = "1780894449946")
    private Long createdAt;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private java.util.List<Telemetry> telemetries;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = System.currentTimeMillis();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
