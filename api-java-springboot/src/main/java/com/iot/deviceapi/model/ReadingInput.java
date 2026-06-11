package com.iot.deviceapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Input model for pushing telemetry data")
public class ReadingInput {

    @JsonProperty("sensor_values")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "{\"temperature\": 24.5, \"humidity\": 60}")
    private Map<String, Object> sensorValues;

    public Map<String, Object> getSensorValues() {
        return sensorValues;
    }

    public void setSensorValues(Map<String, Object> sensorValues) {
        this.sensorValues = sensorValues;
    }
}
