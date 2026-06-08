from fastapi import APIRouter, Depends, HTTPException, status, Path
from sqlalchemy.orm import Session
from typing import List, Annotated

import models, schemas, database

router = APIRouter(
    prefix="/api/devices",
    tags=["Devices"]
)

@router.get("/", response_model=List[schemas.Device], summary="Retrieve a list of devices", description="Returns an array of all registered devices")
def read_devices(db: Session = Depends(database.get_db)):
    devices = db.query(models.Device).all()
    return devices

@router.post("/", response_model=schemas.Device, status_code=status.HTTP_201_CREATED, summary="Create a new device", description="Registers a new IoT device in the system")
def create_device(device: schemas.DeviceInput, db: Session = Depends(database.get_db)):
    db_device = models.Device(
        name=device.name,
        type=device.type,
        status=device.status if device.status else "ACTIVE"
    )
    db.add(db_device)
    db.commit()
    db.refresh(db_device)
    return db_device

@router.get("/{device_id}", response_model=schemas.Device, summary="Get a device by ID", description="Returns detailed information about a specific device")
def read_device(device_id: Annotated[str, Path(example="550e8400-e29b-41d4-a716-446655440000")], db: Session = Depends(database.get_db)):
    device = db.query(models.Device).filter(models.Device.id == device_id).first()
    if device is None:
        raise HTTPException(status_code=404, detail="Device not found")
    return device

@router.put("/{device_id}", response_model=schemas.Device, summary="Update a device", description="Updates the attributes of an existing device")
def update_device(device_id: Annotated[str, Path(example="550e8400-e29b-41d4-a716-446655440000")], device: schemas.DeviceInput, db: Session = Depends(database.get_db)):
    db_device = db.query(models.Device).filter(models.Device.id == device_id).first()
    if db_device is None:
        raise HTTPException(status_code=404, detail="Device not found")
    
    db_device.name = device.name
    if device.type is not None:
        db_device.type = device.type
    if device.status is not None:
        db_device.status = device.status
        
    db.commit()
    db.refresh(db_device)
    return db_device

@router.delete("/{device_id}", status_code=status.HTTP_204_NO_CONTENT, summary="Delete a device", description="Permanently removes a device from the system")
def delete_device(device_id: Annotated[str, Path(example="550e8400-e29b-41d4-a716-446655440000")], db: Session = Depends(database.get_db)):
    db_device = db.query(models.Device).filter(models.Device.id == device_id).first()
    if db_device is None:
        raise HTTPException(status_code=404, detail="Device not found")
    
    db.delete(db_device)
    db.commit()
    return None
