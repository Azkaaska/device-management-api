package com.iot.deviceapi.model;

import jakarta.persistence.*;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "devices")
@Schema(description = "IoT Device metadata simplified schema")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @JsonProperty("id")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    @JsonProperty("name")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Sensor Suhu Ruang Server")
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    @JsonProperty("type")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Thermometer")
    private String type;

    @Column(nullable = false, length = 20)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "active", example = "active")
    private String status = "active";

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
}
