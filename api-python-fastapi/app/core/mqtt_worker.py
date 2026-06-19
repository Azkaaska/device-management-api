import json
from uuid import UUID
import paho.mqtt.client as mqtt
from paho.mqtt.enums import CallbackAPIVersion
from app.config import settings
from app.core.postgres_db import SessionLocal
from app.models.device import Device
from app.models.reading import Reading

class MQTTSubscriberWorker:
    def __init__(self):
        self.client = mqtt.Client(callback_api_version=CallbackAPIVersion.VERSION2)

    def start(self):
        def on_connect(client, userdata, flags, rc, properties=None):
            if rc == 0:
                print(f"MQTT Ingestion: Connected to broker {settings.MQTT_HOST}:{settings.MQTT_PORT}")
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
                
                with SessionLocal() as db:
                    device = db.query(Device).filter(Device.device_id == device_id).first()
                    if not device:
                        print(f"MQTT Ingestion: Unknown device {device_id}, skipping")
                        return
                    
                    Reading.save(device_id, sensor_values, ts)
                    print(f"MQTT Ingestion: Saved telemetry for device {device_id}")
            except Exception as e:
                print(f"MQTT Ingestion Error: {e}")

        self.client.on_connect = on_connect
        self.client.on_message = on_message
        
        self.client.connect(settings.MQTT_HOST, settings.MQTT_PORT, 60)
        self.client.loop_start()

    def stop(self):
        self.client.loop_stop()
        self.client.disconnect()

mqtt_worker = MQTTSubscriberWorker()
