package com.iot.deviceapi.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingKeyTest {

    @Test
    void equals_sameFields_returnsTrue() {
        UUID id = UUID.randomUUID();
        ReadingKey k1 = new ReadingKey(id, "2024-06-01", 1000L);
        ReadingKey k2 = new ReadingKey(id, "2024-06-01", 1000L);

        assertThat(k1).isEqualTo(k2);
    }

    @Test
    void equals_differentTimestamp_returnsFalse() {
        UUID id = UUID.randomUUID();
        ReadingKey k1 = new ReadingKey(id, "2024-06-01", 1000L);
        ReadingKey k2 = new ReadingKey(id, "2024-06-01", 9999L);

        assertThat(k1).isNotEqualTo(k2);
    }

    @Test
    void equals_differentBucket_returnsFalse() {
        UUID id = UUID.randomUUID();
        ReadingKey k1 = new ReadingKey(id, "2024-06-01", 1000L);
        ReadingKey k2 = new ReadingKey(id, "2024-06-02", 1000L);

        assertThat(k1).isNotEqualTo(k2);
    }

    @Test
    void hashCode_equalKeys_haveSameHash() {
        UUID id = UUID.randomUUID();
        ReadingKey k1 = new ReadingKey(id, "2024-06-01", 1000L);
        ReadingKey k2 = new ReadingKey(id, "2024-06-01", 1000L);

        assertThat(k1.hashCode()).isEqualTo(k2.hashCode());
    }

    @Test
    void getters_returnCorrectValues() {
        UUID id = UUID.randomUUID();
        ReadingKey key = new ReadingKey(id, "2024-06-01", 1234567890L);

        assertThat(key.getDeviceId()).isEqualTo(id);
        assertThat(key.getBucketDate()).isEqualTo("2024-06-01");
        assertThat(key.getTsDevice()).isEqualTo(1234567890L);
    }
}
