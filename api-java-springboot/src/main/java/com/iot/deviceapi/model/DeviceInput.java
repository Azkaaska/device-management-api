package com.iot.deviceapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Model input untuk membuat atau memperbarui data perangkat")
public class DeviceInput {

    @JsonProperty("name")
    @Schema(description = "Nama perangkat IoT", requiredMode = Schema.RequiredMode.REQUIRED, example = "Sensor Suhu Ruang Server")
    private String name;

    @JsonProperty("type")
    @Schema(description = "Tipe perangkat (misalnya, Thermometer, Barometer)", requiredMode = Schema.RequiredMode.REQUIRED, example = "Thermometer")
    private String type;
    
    @JsonProperty("status")
    @Schema(description = "Status operasional perangkat", defaultValue = "active", example = "active")
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
