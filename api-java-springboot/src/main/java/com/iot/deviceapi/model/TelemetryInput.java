package com.iot.deviceapi.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({ "device_id", "temperature", "humidity" })
public class TelemetryInput {

    @Schema(example = "22.5")
    private Double temperature;

    @Schema(example = "45")
    private Double humidity;

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
}
