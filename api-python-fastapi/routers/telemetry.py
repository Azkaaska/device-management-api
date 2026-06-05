from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy.exc import IntegrityError
from typing import List, Optional, Any

import models, schemas, database

router = APIRouter(
    prefix="/api/devices",
    tags=["Telemetry"]
)

@router.post("/{device_id}/telemetry", response_model=schemas.Telemetry, status_code=status.HTTP_201_CREATED, summary="Push telemetry to a device", description="Records a new telemetry data point for a specific device")
def create_telemetry(device_id: str, telemetry: schemas.TelemetryCreate, db: Session = Depends(database.get_db)):
    db_telemetry = models.Telemetry(
        device_id=device_id,
        temperature=telemetry.temperature,
        humidity=telemetry.humidity
    )
    db.add(db_telemetry)
    try:
        db.commit()
    except IntegrityError:
        db.rollback()
        raise HTTPException(status_code=404, detail="Device not found")
    
    db.refresh(db_telemetry)
    return db_telemetry

@router.get("/{device_id}/telemetry", summary="Get latest telemetry for a device", description="Retrieves the single most recent telemetry record")
def read_telemetry(device_id: str, db: Session = Depends(database.get_db)):
    db_device_id = db.query(models.Device.id).filter(models.Device.id == device_id).first()
    if db_device_id is None:
        raise HTTPException(status_code=404, detail="Device not found")
        
    telemetry = db.query(models.Telemetry).filter(models.Telemetry.device_id == device_id).order_by(models.Telemetry.ts.desc()).first()
    
    if telemetry:
        return schemas.Telemetry.model_validate(telemetry)
    return {}
