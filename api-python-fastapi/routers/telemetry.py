from fastapi import APIRouter, Depends, HTTPException, status, Path, Query
from sqlalchemy.orm import Session
from typing import List, Optional, Any, Annotated
from uuid import UUID

import schemas
from database import postgres
from models.device import Device
from models.reading import Reading

router = APIRouter(
    prefix="/api/devices",
    tags=["Telemetry"]
)

@router.get("/{device_id}/telemetry", response_model=Any, summary="Get telemetry for a device", description="Retrieves the single most recent telemetry record, or a list of historical readings if start_time and end_time are provided.")
def read_telemetry(
    device_id: Annotated[UUID, Path(example="550e8400-e29b-41d4-a716-446655440000")],
    start_time: Optional[int] = Query(default=None, example=1780894449000),
    end_time: Optional[int] = Query(default=None, example=1780894450000),
    db: Session = Depends(postgres.get_db)
):
    try:
        device_exists = db.query(Device.device_id).filter(Device.device_id == device_id).first()
        if device_exists is None:
            raise HTTPException(status_code=404, detail="Device not found")

        if start_time is not None and end_time is not None:
            readings = Reading.get_historical(device_id, start_time, end_time)
            return readings
        else:
            reading = Reading.get_latest(device_id)
            return reading if reading is not None else {}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

@router.post("/{device_id}/telemetry", response_model=schemas.Reading, status_code=status.HTTP_201_CREATED, summary="Push telemetry to a device", description="Records a new telemetry data point for a specific device")
def create_telemetry(
    device_id: Annotated[UUID, Path(example="550e8400-e29b-41d4-a716-446655440000")],
    telemetry: schemas.ReadingInput,
    db: Session = Depends(postgres.get_db)
):
    try:
        device_exists = db.query(Device.device_id).filter(Device.device_id == device_id).first()
        if device_exists is None:
            raise HTTPException(status_code=404, detail="Device not found")

        res_reading = Reading.save(device_id, telemetry.sensor_values)
        return res_reading
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")
