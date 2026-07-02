import asyncio
from contextlib import asynccontextmanager
from pathlib import Path
from uuid import UUID
from typing import Optional
from fastapi import FastAPI, WebSocket, WebSocketDisconnect, status, HTTPException, Query
from fastapi.responses import FileResponse
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.core.postgres_db import engine, Base
from app.core.cassandra_db import cassandra_db
from app.core.mqtt_worker import mqtt_worker
from app.core.websocket_manager import manager
from app.models.reading import Reading
from app.api.router import api_router

TEMPLATE_FILE = Path(__file__).resolve().parent / "app" / "templates" / "live_view.html"

@asynccontextmanager
async def lifespan(app: FastAPI):
    manager.set_loop(asyncio.get_running_loop())
    Base.metadata.create_all(bind=engine)
    cassandra_db.connect()
    Reading.init_table()
    mqtt_worker.start()
    yield
    mqtt_worker.stop()
    cassandra_db.close()

servers_metadata = [
    {
        "url": "http://localhost:3000", 
        "description": "Local development server"
    }
]

tags_metadata = [
    {
        "name": "Devices",
        "description": "Operations related to managing device attributes"
    },
    {
        "name": "Telemetry",
        "description": "Operations related to pushing and retrieving time-series data"
    }
]

app = FastAPI(
    docs_url="/docs-ui",
    title=settings.APP_TITLE,
    description="API for managing IoT Devices and Telemetry",
    version=settings.VERSION,
    redoc_url=None,
    openapi_tags=tags_metadata,
    servers=servers_metadata,
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origin_regex="https?://.*",
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_router, prefix="/api/v1")

@app.get("/", response_class=FileResponse, summary="Serve Live Telemetry Dashboard")
def read_root():
    if not TEMPLATE_FILE.exists():
        print(TEMPLATE_FILE)
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, 
            detail="Dashboard template file footprint missing from server storage"
        )
    return FileResponse(TEMPLATE_FILE)

@app.websocket("/api/ws")
async def websocket_endpoint(websocket: WebSocket, device_id: Optional[UUID] = Query(None)):
    await manager.connect(device_id, websocket)
    try:
        while True:
            await websocket.receive_text()
    except WebSocketDisconnect:
        manager.disconnect(device_id, websocket)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=3000, reload=True)
