from pydantic import BaseModel, Field
from uuid import UUID
import time

def get_current_unix_ms():
    return time.time_ns() // 1000000

class Reading(BaseModel):
    device_id: UUID = Field(example="550e8400-e29b-41d4-a716-446655440000")
    bucket_date: str = Field(example="2026-06-12")
    ts: int = Field(default_factory=get_current_unix_ms, example=1780894449950)
    sensor_values: dict = Field(example={"temperature": 24.5, "humidity": 60})

    class Config:
        from_attributes = True

class ReadingInput(BaseModel):
    sensor_values: dict = Field(example={"temperature": 24.5, "humidity": 60})
