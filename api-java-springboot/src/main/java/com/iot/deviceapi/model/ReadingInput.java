package com.iot.deviceapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Model input data telemetri yang dikirim oleh perangkat emulator")
public class ReadingInput {

    @JsonProperty("ts")
    @Schema(description = "Timestamp epoch milidetik pembacaan sensor pada perangkat", requiredMode = Schema.RequiredMode.REQUIRED, example = "1717488000000")
    private Long ts;

    @JsonProperty("temperature")
    @Schema(description = "Nilai suhu yang dibaca oleh sensor (Celsius)", requiredMode = Schema.RequiredMode.REQUIRED, example = "28.5")
    private Float temperature;

    @JsonProperty("humidity")
    @Schema(description = "Nilai kelembapan yang dibaca oleh sensor (Persen)", requiredMode = Schema.RequiredMode.REQUIRED, example = "75.2")
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
