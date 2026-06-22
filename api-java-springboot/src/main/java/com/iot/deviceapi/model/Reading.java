package com.iot.deviceapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table("readings")
public class Reading {

    @PrimaryKey
    @JsonIgnore
    private ReadingKey key;

    @Column("ts_receive")
    @JsonProperty("ts_receive")
    private Long tsReceive;

    @Column("temperature")
    private Float temperature;

    @Column("humidity")
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
    public UUID getDeviceId() {
        return key != null ? key.getDeviceId() : null;
    }

    public void setDeviceId(UUID deviceId) {
        if (this.key == null) this.key = new ReadingKey();
        this.key.setDeviceId(deviceId);
    }

    @JsonProperty("bucket_date")
    public String getBucketDate() {
        return key != null ? key.getBucketDate() : null;
    }

    public void setBucketDate(String bucketDate) {
        if (this.key == null) this.key = new ReadingKey();
        this.key.setBucketDate(bucketDate);
    }

    @JsonProperty("ts_device")
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
