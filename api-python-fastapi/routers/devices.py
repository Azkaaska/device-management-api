from fastapi import APIRouter, Depends, HTTPException, status, Path
from sqlalchemy.orm import Session
from sqlalchemy.exc import SQLAlchemyError, IntegrityError
from typing import List, Annotated
from uuid import UUID

import schemas
from database import postgres
from models.device import Device

router = APIRouter(
    prefix="/api/devices",
    tags=["Devices"]
)

@router.get("/", response_model=List[schemas.Device], summary="Retrieve a list of devices", description="Returns an array of all registered devices")
def read_devices(db: Session = Depends(postgres.get_db)):
    try:
        devices = db.query(Device).all()
        return devices
    except SQLAlchemyError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

@router.post("/", response_model=schemas.Device, status_code=status.HTTP_201_CREATED, summary="Create a new device", description="Registers a new IoT device in the system")
def create_device(device: schemas.DeviceInput, db: Session = Depends(postgres.get_db)):
    try:
        db_device = Device(
            device_name=device.device_name,
            device_type=device.device_type,
            status=device.status if device.status else "ACTIVE",
            firmware_version=device.firmware_version,
            device_metadata=device.device_metadata
        )
        db.add(db_device)
        db.commit()
        db.refresh(db_device)
        return db_device
    except IntegrityError as e:
        db.rollback()
        raise HTTPException(status_code=409, detail=f"Conflict: {str(e.orig)}")
    except SQLAlchemyError as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

@router.get("/{device_id}", response_model=schemas.Device, summary="Get a device by ID", description="Returns detailed information about a specific device")
def read_device(device_id: Annotated[UUID, Path(example="550e8400-e29b-41d4-a716-446655440000")], db: Session = Depends(postgres.get_db)):
    try:
        device = db.query(Device).filter(Device.device_id == device_id).first()
        if device is None:
            raise HTTPException(status_code=404, detail="Device not found")
        return device
    except HTTPException:
        raise
    except SQLAlchemyError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

@router.put("/{device_id}", response_model=schemas.Device, summary="Update a device", description="Updates the attributes of an existing device")
def update_device(device_id: Annotated[UUID, Path(example="550e8400-e29b-41d4-a716-446655440000")], device: schemas.DeviceInput, db: Session = Depends(postgres.get_db)):
    try:
        db_device = db.query(Device).filter(Device.device_id == device_id).first()
        if db_device is None:
            raise HTTPException(status_code=404, detail="Device not found")

        db_device.device_name = device.device_name
        db_device.device_type = device.device_type
        if device.status is not None:
            db_device.status = device.status
        db_device.firmware_version = device.firmware_version
        db_device.device_metadata = device.device_metadata

        db.commit()
        db.refresh(db_device)
        return db_device
    except HTTPException:
        raise
    except IntegrityError as e:
        db.rollback()
        raise HTTPException(status_code=409, detail=f"Conflict: {str(e.orig)}")
    except SQLAlchemyError as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

@router.delete("/{device_id}", status_code=status.HTTP_204_NO_CONTENT, summary="Delete a device (Soft Delete)", description="Soft deletes a device by setting its status to INACTIVE")
def delete_device(device_id: Annotated[UUID, Path(example="550e8400-e29b-41d4-a716-446655440000")], db: Session = Depends(postgres.get_db)):
    try:
        db_device = db.query(Device).filter(Device.device_id == device_id).first()
        if db_device is None:
            raise HTTPException(status_code=404, detail="Device not found")

        db_device.status = "INACTIVE"
        db.commit()
        return None
    except HTTPException:
        raise
    except SQLAlchemyError as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")
