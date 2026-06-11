from pydantic import BaseModel, Field
from typing import Optional
from uuid import UUID
import time

def get_current_unix_ms():
    return time.time_ns() // 1000000

# Device Schemas
class Device(BaseModel):
    device_id: UUID = Field(example="550e8400-e29b-41d4-a716-446655440000")
    device_name: str = Field(example="Sensor Suhu Ruang Server")
    device_type: str = Field(example="Thermometer")
    status: str = Field(default="ACTIVE", example="ACTIVE")
    firmware_version: Optional[str] = Field(default="v2.1.0", example="v2.1.0")
    device_metadata: Optional[dict] = Field(default={"floor": 3, "room": "301"}, example={"floor": 3, "room": "301"})
    created_at: int = Field(default_factory=get_current_unix_ms, example=1780894449946)
    updated_at: int = Field(default_factory=get_current_unix_ms, example=1780894449946)

    class Config:
        from_attributes = True

class DeviceInput(BaseModel):
    device_name: str = Field(example="Sensor Suhu Ruang Server")
    device_type: str = Field(example="Thermometer")
    status: Optional[str] = Field(default="ACTIVE", example="ACTIVE")
    firmware_version: Optional[str] = Field(default="v2.1.0", example="v2.1.0")
    device_metadata: Optional[dict] = Field(default={"floor": 3, "room": "301"}, example={"floor": 3, "room": "301"})

# Reading Schemas
class Reading(BaseModel):
    id: int = Field(example=1)
    device_id: UUID = Field(example="550e8400-e29b-41d4-a716-446655440000")
    ts: int = Field(default_factory=get_current_unix_ms, example=1780894449950)
    sensor_values: dict = Field(example={"temperature": 24.5, "humidity": 60})

    class Config:
        from_attributes = True

class ReadingInput(BaseModel):
    sensor_values: dict = Field(example={"temperature": 24.5, "humidity": 60})
