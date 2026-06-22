package com.iot.deviceapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Input model for creating or updating a simplified device")
public class DeviceInput {

    @JsonProperty("name")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Sensor Suhu Ruang Server")
    private String name;

    @JsonProperty("type")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Thermometer")
    private String type;

    @Schema(defaultValue = "active", example = "active")
    private String status = "active";

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
