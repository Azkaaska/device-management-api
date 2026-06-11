from fastapi import APIRouter, Depends, HTTPException, status, Path, Query
from sqlalchemy.orm import Session
from typing import List, Optional, Any, Annotated
from uuid import UUID

import models, schemas, database

router = APIRouter(
    prefix="/api/devices",
    tags=["Telemetry"]
)

@router.get("/{device_id}/telemetry", response_model=Any, summary="Get telemetry for a device", description="Retrieves the single most recent telemetry record, or a list of historical readings if start_time and end_time are provided.")
def read_telemetry(
    device_id: Annotated[UUID, Path(example="550e8400-e29b-41d4-a716-446655440000")],
    start_time: Optional[int] = Query(default=None, example=1780894449000),
    end_time: Optional[int] = Query(default=None, example=1780894450000),
    db: Session = Depends(database.get_db)
):
    device_exists = db.query(models.Device.device_id).filter(models.Device.device_id == device_id).first()
    if device_exists is None:
        raise HTTPException(status_code=404, detail="Device not found")
        
    if start_time is not None and end_time is not None:
        readings = db.query(models.Reading).filter(
            models.Reading.device_id == device_id,
            models.Reading.ts >= start_time,
            models.Reading.ts <= end_time
        ).order_by(models.Reading.ts.desc()).all()
        return [schemas.Reading.model_validate(r) for r in readings]
    else:
        reading = db.query(models.Reading).filter(
            models.Reading.device_id == device_id
        ).order_by(models.Reading.ts.desc()).first()
        if reading:
            return schemas.Reading.model_validate(reading)
        return {}

@router.post("/{device_id}/telemetry", response_model=schemas.Reading, status_code=status.HTTP_201_CREATED, summary="Push telemetry to a device", description="Records a new telemetry data point for a specific device")
def create_telemetry(
    device_id: Annotated[UUID, Path(example="550e8400-e29b-41d4-a716-446655440000")],
    telemetry: schemas.ReadingInput,
    db: Session = Depends(database.get_db)
):
    device_exists = db.query(models.Device.device_id).filter(models.Device.device_id == device_id).first()
    if device_exists is None:
        raise HTTPException(status_code=404, detail="Device not found")

    db_reading = models.Reading(
        device_id=device_id,
        sensor_values=telemetry.sensor_values
    )
    db.add(db_reading)
    db.commit()
    db.refresh(db_reading)
    return db_reading
