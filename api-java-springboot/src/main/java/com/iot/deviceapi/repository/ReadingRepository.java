package com.iot.deviceapi.repository;

import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.model.ReadingKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReadingRepository extends CassandraRepository<Reading, ReadingKey> {
    
    @Query("SELECT * FROM readings WHERE device_id = ?0 AND bucket_date = ?1 LIMIT 1")
    List<Reading> findLatestByDeviceAndBucket(UUID deviceId, String bucketDate);
    
    @Query("SELECT * FROM readings WHERE device_id = ?0 AND bucket_date = ?1 AND ts_device >= ?2 AND ts_device <= ?3")
    List<Reading> findByDeviceAndBucketAndTsRange(UUID deviceId, String bucketDate, Long start, Long end);
}
