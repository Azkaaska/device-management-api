package com.iot.deviceapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Flat input payload from emulator device")
public class ReadingInput {

    @JsonProperty("ts")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1717488000000")
    private Long ts;

    @JsonProperty("temperature")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "28.5")
    private Float temperature;

    @JsonProperty("humidity")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "75.2")
    private Float humidity;

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }
}
