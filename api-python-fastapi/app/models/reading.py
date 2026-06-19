import json
import time
from uuid import UUID
from datetime import datetime, timedelta, timezone
from app.core.cassandra_db import cassandra_db

TZ_LOCAL = timezone(timedelta(hours=7))

class Reading:
    @staticmethod
    def init_table():
        cassandra_db.session.execute("""
            CREATE TABLE IF NOT EXISTS readings (
                device_id uuid,
                bucket_date text,
                ts bigint,
                sensor_values text,
                PRIMARY KEY ((device_id, bucket_date), ts)
            ) WITH CLUSTERING ORDER BY (ts DESC);
        """)

    @staticmethod
    def get_bucket_date(ts_ms: int) -> str:
        dt = datetime.fromtimestamp(ts_ms / 1000.0, tz=TZ_LOCAL)
        return dt.strftime('%Y-%m-%d')

    @classmethod
    def save(cls, device_id: UUID, sensor_values: dict, ts: int = None) -> dict:
        if ts is None:
            ts = int(time.time_ns() // 1000000)
        bucket_date = cls.get_bucket_date(ts)
        sensor_values_str = json.dumps(sensor_values)
        
        query = """
            INSERT INTO readings (device_id, bucket_date, ts, sensor_values)
            VALUES (%s, %s, %s, %s)
        """
        cassandra_db.session.execute(query, (device_id, bucket_date, ts, sensor_values_str))
        return {
            "device_id": device_id,
            "bucket_date": bucket_date,
            "ts": ts,
            "sensor_values": sensor_values
        }

    @classmethod
    def get_latest(cls, device_id: UUID) -> dict:
        now_ms = int(time.time_ns() // 1000000)
        now_dt = datetime.fromtimestamp(now_ms / 1000.0, tz=TZ_LOCAL)
        
        for i in range(8):
            bucket_date = (now_dt - timedelta(days=i)).strftime('%Y-%m-%d')
            query = """
                SELECT device_id, bucket_date, ts, sensor_values 
                FROM readings 
                WHERE device_id = %s AND bucket_date = %s 
                LIMIT 1
            """
            rows = cassandra_db.session.execute(query, (device_id, bucket_date))
            for row in rows:
                return {
                    "device_id": row.device_id,
                    "bucket_date": row.bucket_date,
                    "ts": row.ts,
                    "sensor_values": json.loads(row.sensor_values)
                }
        return None

    @classmethod
    def get_historical(cls, device_id: UUID, start_time: int, end_time: int) -> list:
        start_dt = datetime.fromtimestamp(start_time / 1000.0, tz=TZ_LOCAL)
        end_dt = datetime.fromtimestamp(end_time / 1000.0, tz=TZ_LOCAL)
        
        bucket_dates = []
        curr = start_dt.date()
        while curr <= end_dt.date():
            bucket_dates.append(curr.strftime('%Y-%m-%d'))
            curr += timedelta(days=1)
            
        readings = []
        for bucket in bucket_dates:
            query = """
                SELECT device_id, bucket_date, ts, sensor_values 
                FROM readings 
                WHERE device_id = %s AND bucket_date = %s AND ts >= %s AND ts <= %s
            """
            rows = cassandra_db.session.execute(query, (device_id, bucket, start_time, end_time))
            for row in rows:
                readings.append({
                    "device_id": row.device_id,
                    "bucket_date": row.bucket_date,
                    "ts": row.ts,
                    "sensor_values": json.loads(row.sensor_values)
                })
                
        readings.sort(key=lambda r: r['ts'], reverse=True)
        return readings
