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

# Each device holds its own topic (place hierarchy) and type for sensor generation
DEVICES = [
    {
        "deviceId": "550e8400-e29b-41d4-a716-446655440001",
        "deviceType": "thermometer",
        "topic": "buildingA/floor2/550e8400-e29b-41d4-a716-446655440001"
    },
    {
        "deviceId": "550e8400-e29b-41d4-a716-446655440002",
        "deviceType": "energymeter",
        "topic": "plantB/chiller4/550e8400-e29b-41d4-a716-446655440002"
    },
    {
        "deviceId": "550e8400-e29b-41d4-a716-446655440003",
        "deviceType": "waterflow",
        "topic": "plantB/coolingTower/550e8400-e29b-41d4-a716-446655440003"
    }
]


def run_device_emulator(device):
    client = mqtt.Client(callback_api_version=CallbackAPIVersion.VERSION2)
    try:
        client.connect(MQTT_HOST, MQTT_PORT, 60)
        client.loop_start()
        print(f"[{device['deviceType']}] Connected to MQTT Broker ({MQTT_HOST}:{MQTT_PORT})")
    except Exception as e:
        print(f"[{device['deviceType']}] MQTT Connection Failed: {e}")
        return

    energy_accumulated = random.uniform(10.0, 50.0)
    water_volume_accumulated = random.uniform(100.0, 500.0)

    while True:
        try:
            ts = int(time.time_ns() // 1_000_000)  # Unix ms timestamp

            if device["deviceType"] == "thermometer":
                sensor_values = {
                    "temperature": round(random.uniform(20.0, 35.0), 2),
                    "humidity": round(random.uniform(40.0, 80.0), 2)
                }
            elif device["deviceType"] == "energymeter":
                voltage = round(random.uniform(220.0, 240.0), 1)
                current = round(random.uniform(0.5, 8.0), 2)
                active_power = round((voltage * current * 0.9) / 1000.0, 3)
                energy_accumulated += round(active_power * (2 / 3600.0), 5)
                sensor_values = {
                    "voltage": voltage,
                    "current": current,
                    "active_power": active_power,
                    "total_energy": round(energy_accumulated, 4)
                }
            elif device["deviceType"] == "waterflow":
                flow_rate = round(random.uniform(5.0, 25.0), 2)
                water_volume_accumulated += round(flow_rate * (2 / 60.0), 2)
                sensor_values = {
                    "flow_rate": flow_rate,
                    "total_volume": round(water_volume_accumulated, 2)
                }
            else:
                sensor_values = {}

            payload = {"ts": ts, "sensor_values": sensor_values}

            client.publish(device["topic"], json.dumps(payload), qos=1)
            print(f"[{device['deviceType']}] → {device['topic']} | {payload}")
            time.sleep(2)

        except Exception as e:
            print(f"[{device['deviceType']}] Emulator error: {e}")
            break

    client.loop_stop()
    client.disconnect()


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
