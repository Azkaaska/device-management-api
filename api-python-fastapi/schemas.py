from pydantic import BaseModel, Field
from typing import Optional
from uuid import UUID

# Telemetry Schemas
class TelemetryBase(BaseModel):
    temperature: Optional[float] = None
    humidity: Optional[float] = None

class TelemetryCreate(TelemetryBase):
    pass

class Telemetry(TelemetryBase):
    device_id: UUID = Field(alias="deviceId")
    ts: int

    class Config:
        from_attributes = True
        populate_by_name = True

# Device Schemas
class DeviceBase(BaseModel):
    name: str
    type: Optional[str] = None
    status: Optional[str] = "ACTIVE"

class DeviceCreate(DeviceBase):
    pass

class DeviceUpdate(BaseModel):
    name: Optional[str] = None
    type: Optional[str] = None
    status: Optional[str] = None

class Device(DeviceBase):
    id: UUID
    created_at: int = Field(alias="createdAt")

    class Config:
        from_attributes = True
        populate_by_name = True
