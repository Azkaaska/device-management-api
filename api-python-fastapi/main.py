from database.cassandra import get_cassandra_session
from database.postgres import engine, Base
from fastapi import FastAPI
from models.device import Device
from routers import devices, telemetry

# Create database tables (PostgreSQL)
Base.metadata.create_all(bind=engine)
# Initialize Cassandra Session
get_cassandra_session()

import json
import os
import threading
import time
import paho.mqtt.client as mqtt
from database.postgres import SessionLocal
from models.reading import Reading
from paho.mqtt.enums import CallbackAPIVersion
from uuid import UUID

def start_mqtt_subscriber():
    host = os.getenv("MQTT_HOST", "127.0.0.1")
    port = int(os.getenv("MQTT_PORT", 1883))
    client = mqtt.Client(callback_api_version=CallbackAPIVersion.VERSION2)
    
    def on_connect(client, userdata, flags, rc, properties=None):
        if rc == 0:
            print(f"MQTT Ingestion: Connected to broker {host}:{port}")
            client.subscribe("+/+/+")
        else:
            print(f"MQTT Ingestion: Connection failed with code {rc}")

    def on_message(client, userdata, msg):
        try:
            parts = msg.topic.split('/')
            if len(parts) != 3:
                print(f"MQTT Ingestion: Invalid topic structure ({msg.topic})")
                return
            
            try:
                device_id = UUID(parts[2])
            except ValueError:
                print(f"MQTT Ingestion: Invalid UUID format in topic token '{parts[2]}'")
                return

            try:
                payload = json.loads(msg.payload.decode('utf-8'))
            except (json.JSONDecodeError, UnicodeDecodeError):
                print(f"MQTT Ingestion: Corrupted/Non-JSON payload received on {msg.topic}")
                return
            
            ts = payload.get("ts")
            sensor_values = payload.get("sensor_values")
            
            if ts is None or sensor_values is None:
                print(f"MQTT Ingestion: Missing 'ts' or 'sensor_values' in payload")
                return
            
            db = SessionLocal()
            try:
                device = db.query(Device).filter(Device.device_id == device_id).first()
                if not device:
                    print(f"MQTT Ingestion: Unknown device {device_id}, skipping")
                    return
                
                Reading.save(device_id, sensor_values, ts)
                print(f"MQTT Ingestion: Saved telemetry for device {device_id}")
            finally:
                db.close()
        except Exception as e:
            print(f"MQTT Ingestion Error: {e}")
            
    client.on_connect = on_connect
    client.on_message = on_message
    
    while True:
        try:
            client.connect(host, port, 60)
            client.loop_forever()
        except Exception as e:
            print(f"MQTT Ingestion Client Error: {e}. Reconnecting in 5 seconds...")
            time.sleep(5)

threading.Thread(target=start_mqtt_subscriber, daemon=True).start()

servers_metadata = [
    {
        "url": "http://localhost:3000", 
        "description": "Local development server"
    }
]
tags_metadata = [
    {
        "name": "Devices",
        "description": "Operations related to managing device attributes"
    },
    {
        "name": "Telemetry",
        "description": "Operations related to pushing and retrieving time-series data"
    }
]

app = FastAPI(
    docs_url="/docs-ui",
    title="IoT Device & Telemetry API",
    description="API for managing IoT Devices and Telemetry",
    version="1.0.0",
    redoc_url=None,
    openapi_tags=tags_metadata,
    servers=servers_metadata
)

app.include_router(devices.router)
app.include_router(telemetry.router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=3000, reload=True)
