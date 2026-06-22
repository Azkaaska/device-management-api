import os
import time
import json
import random
import threading
import paho.mqtt.client as mqtt
from paho.mqtt.enums import CallbackAPIVersion
from dotenv import load_dotenv

load_dotenv()

MQTT_HOST = os.getenv("MQTT_HOST", "127.0.0.1")
MQTT_PORT = int(os.getenv("MQTT_PORT", 1883))

DEVICES = [
    {
        "deviceId": "550e8400-e29b-41d4-a716-446655440001",
        "deviceType": "thermometer",
        "topic": "buildingA/floor1/550e8400-e29b-41d4-a716-446655440001"
    },
    {
        "deviceId": "550e8400-e29b-41d4-a716-446655440002",
        "deviceType": "thermometer",
        "topic": "buildingA/floor2/550e8400-e29b-41d4-a716-446655440002"
    },
    {
        "deviceId": "550e8400-e29b-41d4-a716-446655440003",
        "deviceType": "thermometer",
        "topic": "buildingA/floor3/550e8400-e29b-41d4-a716-446655440003"
    }
]

def run_device_emulator(device):
    client = mqtt.Client(callback_api_version=CallbackAPIVersion.VERSION2)
    connection_state = {"is_connected": False}
    offline_buffer = []

    def on_connect(client, userdata, flags, rc, properties=None):
        if rc == 0:
            print(f"[{device['deviceType']}] Connected to MQTT Broker.")
            connection_state["is_connected"] = True
        else:
            print(f"[{device['deviceType']}] Connection failed with code {rc}")
            connection_state["is_connected"] = False

    def on_disconnect(client, userdata, flags, rc, properties=None):
        print(f"[{device['deviceType']}] Disconnected from MQTT Broker.")
        connection_state["is_connected"] = False

    client.on_connect = on_connect
    client.on_disconnect = on_disconnect

    connected = False
    while not connected:
        try:
            client.connect(MQTT_HOST, MQTT_PORT, 60)
            client.loop_start()
            connected = True
        except Exception as e:
            print(f"[{device['deviceType']}] Broker unreachable: {e}. Retrying in 5s...")
            time.sleep(5)

    while True:
        try:
            ts = int(time.time_ns() // 1_000_000)

            payload = {
                "ts": ts,
                "temperature": round(random.uniform(20.0, 35.0), 1),
                "humidity": round(random.uniform(40.0, 80.0), 1)
            }

            try:
                serialized_payload = json.dumps(payload)
            except Exception as parse_err:
                print(f"[{device['deviceType']}] Serialization failed: {parse_err}")
                time.sleep(2)
                continue

            if connection_state["is_connected"]:
                while offline_buffer:
                    buffered_msg = offline_buffer.pop(0)
                    print(f"[{device['deviceType']}] [Flushing Buffer] → {device['topic']}")
                    client.publish(device["topic"], buffered_msg, qos=1)
                
                client.publish(device["topic"], serialized_payload, qos=1)
                print(f"[{device['deviceType']}] → {device['topic']} | {payload}")
            else:
                print(f"[{device['deviceType']}] Connection lost. Buffering data telemetry...")
                offline_buffer.append(serialized_payload)
                if len(offline_buffer) > 5000:
                    offline_buffer.pop(0)

            time.sleep(2)

        except Exception as e:
            print(f"[{device['deviceType']}] Emulator loop error: {e}")
            time.sleep(2)

if __name__ == "__main__":
    print("Starting Standalone Multi-Device MQTT Emulator...")
    threads = []
    for dev in DEVICES:
        t = threading.Thread(target=run_device_emulator, args=(dev,), daemon=True)
        threads.append(t)
        t.start()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\nStopping MQTT Emulator.")
