from fastapi import APIRouter, Depends, HTTPException, status, Path, Query
from sqlalchemy.orm import Session
from typing import List, Optional, Annotated
from uuid import UUID

from app.api import deps
from app.models.device import Device
from app.models.reading import Reading
from app.schemas import reading as reading_schemas
from app.core.websocket_manager import manager

router = APIRouter(prefix="/devices", tags=["Telemetry"])

@router.get("/{device_id}/telemetry", response_model=List[reading_schemas.Reading], summary="Get telemetry for a device")
def read_telemetry(
    device_id: Annotated[UUID, Path(example="550e8400-e29b-41d4-a716-446655440000")],
    start_time: Optional[int] = Query(default=None, example=1717488000000),
    end_time: Optional[int] = Query(default=None, example=1717488005000),
    page: int = Query(default=1, ge=1, description="The page number to retrieve (1-indexed)"),
    limit: int = Query(default=100, ge=1, le=500, description="The maximum number of telemetry items to return per page"),
    db: Session = Depends(deps.get_db)
):
    try:
        device_exists = db.query(Device.id).filter(Device.id == device_id).first()
        if device_exists is None:
            raise HTTPException(status_code=404, detail="Device not found")

        if start_time is not None and end_time is not None:
            return Reading.get_historical(
                device_id=device_id, 
                start_time=start_time, 
                end_time=end_time, 
                page=page, 
                limit=limit
            )
        else:
            reading = Reading.get_latest(device_id)
            return reading if reading is not None else {}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

@router.post("/{device_id}/telemetry", response_model=reading_schemas.Reading, status_code=status.HTTP_201_CREATED, summary="Push telemetry to a device")
async def create_telemetry(
    device_id: Annotated[UUID, Path(example="550e8400-e29b-41d4-a716-446655440000")],
    telemetry: reading_schemas.ReadingInput,
    db: Session = Depends(deps.get_db)
):
    try:
        device_exists = db.query(Device.id).filter(Device.id == device_id).first()
        if device_exists is None:
            raise HTTPException(status_code=404, detail="Device not found")

        saved_reading = Reading.save(device_id, telemetry.ts, telemetry.temperature, telemetry.humidity)
        
        broadcast_payload = {**saved_reading, "device_id": str(device_id)}
        await manager.broadcast(device_id, broadcast_payload)
        
        return saved_reading
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")
