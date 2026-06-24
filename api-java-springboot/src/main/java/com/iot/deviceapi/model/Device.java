package com.iot.deviceapi.model;

import jakarta.persistence.*;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "devices")
@Schema(description = "Skema metadata untuk Perangkat IoT")
@JsonPropertyOrder({ "id", "name", "type", "status" })
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @JsonProperty("id")
    @Schema(description = "ID unik perangkat (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    @JsonProperty("name")
    @Schema(description = "Nama perangkat IoT", example = "Sensor Suhu Ruang Server")
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    @JsonProperty("type")
    @Schema(description = "Tipe perangkat (misalnya, Thermometer, Barometer)", example = "Thermometer")
    private String type;
    
    @Column(name = "status", nullable = false, length = 20)
    @JsonProperty("status")
    @Schema(description = "Status operasional perangkat", defaultValue = "active", example = "active")
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
