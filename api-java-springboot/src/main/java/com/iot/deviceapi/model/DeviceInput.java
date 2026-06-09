package com.iot.deviceapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class DeviceInput {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Living Room Sensor")
    private String name;

    @Schema(example = "TEMP_HUMIDITY")
    private String type;

    @Schema(defaultValue = "ACTIVE", example = "ACTIVE")
    private String status = "ACTIVE";

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
