from fastapi import FastAPI
import models
from database import engine
from routers import devices, telemetry

# Create database tables (in-memory SQLite)
models.Base.metadata.create_all(bind=engine)

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
    docs_url="/api-docs",
    title="IoT Device & Telemetry API",
    description="API for managing IoT Devices and Telemetry",
    version="1.0.0",
    redoc_url=None,
    openapi_tags=tags_metadata,
    servers=servers_metadata
)

app.include_router(devices.router)
app.include_router(telemetry.router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=3000, reload=True)
