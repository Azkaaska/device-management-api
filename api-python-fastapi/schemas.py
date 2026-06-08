from pydantic import BaseModel, Field
from typing import Optional
from uuid import UUID
import time

def get_current_unix():
    return int(time.time() * 1000)

# Device Schemas
class Device(BaseModel):
    id: UUID = Field(example="550e8400-e29b-41d4-a716-446655440000")
    name: str = Field(example="Living Room Sensor")
    type: Optional[str] = Field(default=None, example="TEMP_HUMIDITY")
    status: str = "ACTIVE"
    created_at: int = get_current_unix()

    class Config:
        from_attributes = True
        populate_by_name = True

class DeviceInput(BaseModel):
    name: str = Field(example="Living Room Sensor")
    type: Optional[str] = Field(default=None, example="TEMP_HUMIDITY")
    status: Optional[str] = "ACTIVE"

# Telemetry Schemas
class Telemetry(BaseModel):
    id: int = Field(example=1)
    device_id: UUID = Field(example="550e8400-e29b-41d4-a716-446655440000")
    temperature: Optional[float] = Field(default=None, example=22.5)
    humidity: Optional[float] = Field(default=None, example=45)
    ts: int = get_current_unix()

    class Config:
        from_attributes = True
        populate_by_name = True

class TelemetryInput(BaseModel):
    temperature: Optional[float] = Field(default=None, example=22.5)
    humidity: Optional[float] = Field(default=None, example=45)
