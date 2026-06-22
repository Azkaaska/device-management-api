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
                ts_device bigint,
                ts_receive bigint,
                temperature float,
                humidity float,
                PRIMARY KEY ((device_id, bucket_date), ts_device)
            ) WITH CLUSTERING ORDER BY (ts_device DESC);
        """)

    @staticmethod
    def get_bucket_date(ts_ms: int) -> str:
        dt = datetime.fromtimestamp(ts_ms / 1000.0, tz=TZ_LOCAL)
        return dt.strftime('%Y-%m-%d')

    @classmethod
    def save(cls, device_id: UUID, ts_device: int, temperature: float, humidity: float) -> dict:
        ts_receive = int(time.time_ns() // 1000000)
        bucket_date = cls.get_bucket_date(ts_device)
        
        query = """
            INSERT INTO readings (device_id, bucket_date, ts_device, ts_receive, temperature, humidity)
            VALUES (%s, %s, %s, %s, %s, %s)
        """
        cassandra_db.session.execute(query, (device_id, bucket_date, ts_device, ts_receive, temperature, humidity))
        return {
            "device_id": device_id,
            "bucket_date": bucket_date,
            "ts_device": ts_device,
            "ts_receive": ts_receive,
            "temperature": temperature,
            "humidity": humidity
        }

    @classmethod
    def get_latest(cls, device_id: UUID) -> dict:
        now_ms = int(time.time_ns() // 1000000)
        now_dt = datetime.fromtimestamp(now_ms / 1000.0, tz=TZ_LOCAL)
        
        for i in range(8):
            bucket_date = (now_dt - timedelta(days=i)).strftime('%Y-%m-%d')
            query = """
                SELECT device_id, bucket_date, ts_device, ts_receive, temperature, humidity 
                FROM readings 
                WHERE device_id = %s AND bucket_date = %s 
                LIMIT 1
            """
            rows = cassandra_db.session.execute(query, (device_id, bucket_date))
            for row in rows:
                return {
                    "device_id": row.device_id,
                    "bucket_date": row.bucket_date,
                    "ts_device": row.ts_device,
                    "ts_receive": row.ts_receive,
                    "temperature": row.temperature,
                    "humidity": row.humidity
                }
        return None

    @classmethod
    def get_historical(cls, device_id: UUID, start_time: int, end_time: int, page: int = 1, limit: int = 100) -> list:
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
                SELECT device_id, bucket_date, ts_device, ts_receive, temperature, humidity 
                FROM readings 
                WHERE device_id = %s AND bucket_date = %s AND ts_device >= %s AND ts_device <= %s
            """
            rows = cassandra_db.session.execute(query, (device_id, bucket, start_time, end_time))
            for row in rows:
                readings.append({
                    "device_id": row.device_id,
                    "bucket_date": row.bucket_date,
                    "ts_device": row.ts_device,
                    "ts_receive": row.ts_receive,
                    "temperature": row.temperature,
                    "humidity": row.humidity
                })
                
        readings.sort(key=lambda r: r['ts_device'], reverse=True)
        
        start_idx = (page - 1) * limit
        end_idx = start_idx + limit
        return readings[start_idx:end_idx]
