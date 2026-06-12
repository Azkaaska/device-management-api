package com.iot.deviceapi.model;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@PrimaryKeyClass
public class ReadingKey implements Serializable {

    @PrimaryKeyColumn(name = "device_id", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    private UUID deviceId;

    @PrimaryKeyColumn(name = "bucket_date", type = PrimaryKeyType.PARTITIONED, ordinal = 1)
    private String bucketDate;

    @PrimaryKeyColumn(name = "ts", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING, ordinal = 2)
    private Long ts;

    public ReadingKey() {}

    public ReadingKey(UUID deviceId, String bucketDate, Long ts) {
        this.deviceId = deviceId;
        this.bucketDate = bucketDate;
        this.ts = ts;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public String getBucketDate() {
        return bucketDate;
    }

    public void setBucketDate(String bucketDate) {
        this.bucketDate = bucketDate;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReadingKey)) return false;
        ReadingKey that = (ReadingKey) o;
        return Objects.equals(deviceId, that.deviceId) &&
                Objects.equals(bucketDate, that.bucketDate) &&
                Objects.equals(ts, that.ts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, bucketDate, ts);
    }
}
