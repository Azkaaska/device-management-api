const cassandra = require('../database/cassandra');

function getBucketDate(ts) {
  const tzOffset = 7 * 60 * 60 * 1000;
  const localDate = new Date(Number(ts) + tzOffset);
  const yyyy = localDate.getUTCFullYear();
  const mm = String(localDate.getUTCMonth() + 1).padStart(2, '0');
  const dd = String(localDate.getUTCDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

class Reading {
  static async save(deviceId, sensorValues, ts) {
    const client = await cassandra.getSession();
    const timestamp = ts || Date.now();
    const bucketDate = getBucketDate(timestamp);
    const sensorValuesStr = JSON.stringify(sensorValues);

    const query = 'INSERT INTO readings (device_id, bucket_date, ts, sensor_values) VALUES (?, ?, ?, ?)';
    await client.execute(query, [deviceId, bucketDate, timestamp, sensorValuesStr], { prepare: true });

    return {
      device_id: deviceId,
      bucket_date: bucketDate,
      ts: timestamp,
      sensor_values: sensorValues
    };
  }

  static async getLatest(deviceId) {
    const client = await cassandra.getSession();
    const now = Date.now();
    
    // Look back up to 8 days
    for (let i = 0; i < 8; i++) {
      const checkTime = now - i * 24 * 60 * 60 * 1000;
      const bucketDate = getBucketDate(checkTime);

      const query = 'SELECT device_id, bucket_date, ts, sensor_values FROM readings WHERE device_id = ? AND bucket_date = ? LIMIT 1';
      const result = await client.execute(query, [deviceId, bucketDate], { prepare: true });
      if (result.rows.length > 0) {
        const row = result.rows[0];
        return {
          device_id: row.device_id,
          bucket_date: row.bucket_date,
          ts: Number(row.ts),
          sensor_values: JSON.parse(row.sensor_values)
        };
      }
    }
    return null;
  }

  static async getHistorical(deviceId, startTime, endTime) {
    const client = await cassandra.getSession();
    const startLocal = new Date(Number(startTime) + 7 * 60 * 60 * 1000);
    const endLocal = new Date(Number(endTime) + 7 * 60 * 60 * 1000);

    const bucketDates = [];
    let current = new Date(startLocal);
    current.setUTCHours(0, 0, 0, 0);
    const endCompare = new Date(endLocal);
    endCompare.setUTCHours(0, 0, 0, 0);

    while (current <= endCompare) {
      const yyyy = current.getUTCFullYear();
      const mm = String(current.getUTCMonth() + 1).padStart(2, '0');
      const dd = String(current.getUTCDate()).padStart(2, '0');
      bucketDates.push(`${yyyy}-${mm}-${dd}`);
      current.setUTCDate(current.getUTCDate() + 1);
    }

    const allReadings = [];
    for (const bucket of bucketDates) {
      const query = 'SELECT device_id, bucket_date, ts, sensor_values FROM readings WHERE device_id = ? AND bucket_date = ? AND ts >= ? AND ts <= ?';
      const result = await client.execute(query, [deviceId, bucket, startTime, endTime], { prepare: true });
      for (const row of result.rows) {
        allReadings.push({
          device_id: row.device_id,
          bucket_date: row.bucket_date,
          ts: Number(row.ts),
          sensor_values: JSON.parse(row.sensor_values)
        });
      }
    }

    allReadings.sort((a, b) => b.ts - a.ts);
    return allReadings;
  }
}

module.exports = Reading;
