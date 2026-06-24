package com.iot.deviceapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table("readings")
@Schema(description = "Skema data telemetri (pembacaan sensor) perangkat")
@JsonPropertyOrder({ "device_id", "bucket_date", "ts_device", "ts_receive", "temperature", "humidity" })
public class Reading {

    @PrimaryKey
    @JsonIgnore
    private ReadingKey key;

    @Column("ts_receive")
    @JsonProperty("ts_receive")
    @Schema(description = "Timestamp epoch milidetik saat server menerima data telemetri", example = "1717488005000")
    private Long tsReceive;
    
    @Column("temperature")
    @JsonProperty("temperature")
    @Schema(description = "Nilai suhu dalam derajat Celsius", example = "28.5")
    private Float temperature;
    
    @Column("humidity")
    @JsonProperty("humidity")
    @Schema(description = "Nilai kelembapan dalam persentase", example = "75.2")
    private Float humidity;

    public Reading() {
        this.key = new ReadingKey();
    }

    public ReadingKey getKey() {
        return key;
    }

    public void setKey(ReadingKey key) {
        this.key = key;
    }

    @JsonProperty("device_id")
    @Schema(description = "ID unik perangkat (UUID) terkait", example = "550e8400-e29b-41d4-a716-446655440000")
    public UUID getDeviceId() {
        return key != null ? key.getDeviceId() : null;
    }

    public void setDeviceId(UUID deviceId) {
        if (this.key == null) this.key = new ReadingKey();
        this.key.setDeviceId(deviceId);
    }

    @JsonProperty("bucket_date")
    @Schema(description = "Tanggal pengelompokan data untuk partisi Cassandra (format: YYYY-MM-DD)", example = "2026-06-24")
    public String getBucketDate() {
        return key != null ? key.getBucketDate() : null;
    }

    public void setBucketDate(String bucketDate) {
        if (this.key == null) this.key = new ReadingKey();
        this.key.setBucketDate(bucketDate);
    }

    @JsonProperty("ts_device")
    @Schema(description = "Timestamp epoch milidetik dari perangkat ketika sensor dibaca", example = "1717488000000")
    public Long getTsDevice() {
        return key != null ? key.getTsDevice() : null;
    }

    public void setTsDevice(Long tsDevice) {
        if (this.key == null) this.key = new ReadingKey();
        this.key.setTsDevice(tsDevice);
    }

    public Long getTsReceive() {
        return tsReceive;
    }

    public void setTsReceive(Long tsReceive) {
        this.tsReceive = tsReceive;
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
