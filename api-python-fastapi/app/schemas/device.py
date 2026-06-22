from pydantic import BaseModel, Field
from typing import Optional
from uuid import UUID

class Device(BaseModel):
    id: UUID = Field(example="550e8400-e29b-41d4-a716-446655440000")
    name: str = Field(example="Sensor Suhu Ruang Server")
    type: str = Field(example="Thermometer")
    status: str = Field(default="active", example="active")

    class Config:
        from_attributes = True

class DeviceInput(BaseModel):
    name: str = Field(example="Sensor Suhu Ruang Server")
    type: str = Field(example="Thermometer")
    status: Optional[str] = Field(default="active", example="active")
