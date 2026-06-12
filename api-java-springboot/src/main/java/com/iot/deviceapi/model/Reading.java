package com.iot.deviceapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Map;
import java.util.UUID;

@Table("readings")
public class Reading {

    @PrimaryKey
    @JsonIgnore
    private ReadingKey key;

    @Column("sensor_values")
    private String sensorValuesStr;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Reading() {
        this.key = new ReadingKey();
    }

    public Reading(ReadingKey key, String sensorValuesStr) {
        this.key = key;
        this.sensorValuesStr = sensorValuesStr;
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
        if (this.key == null) {
            this.key = new ReadingKey();
        }
        this.key.setDeviceId(deviceId);
    }

    @JsonProperty("bucket_date")
    public String getBucketDate() {
        return key != null ? key.getBucketDate() : null;
    }

    public void setBucketDate(String bucketDate) {
        if (this.key == null) {
            this.key = new ReadingKey();
        }
        this.key.setBucketDate(bucketDate);
    }

    @JsonProperty("ts")
    public Long getTs() {
        return key != null ? key.getTs() : null;
    }

    public void setTs(Long ts) {
        if (this.key == null) {
            this.key = new ReadingKey();
        }
        this.key.setTs(ts);
    }

    @JsonProperty("sensor_values")
    public Map<String, Object> getSensorValues() {
        try {
            if (sensorValuesStr == null) return null;
            return objectMapper.readValue(sensorValuesStr, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    public void setSensorValues(Map<String, Object> sensorValues) {
        try {
            this.sensorValuesStr = objectMapper.writeValueAsString(sensorValues);
        } catch (Exception e) {
            this.sensorValuesStr = "{}";
        }
    }
}
