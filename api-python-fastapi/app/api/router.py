from fastapi import APIRouter
from app.api.endpoints import devices, telemetry

api_router = APIRouter()
api_router.include_router(devices.router)
api_router.include_router(telemetry.router)
