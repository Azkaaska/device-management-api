from fastapi import FastAPI
from contextlib import asynccontextmanager
import models
from database import engine
from routers import devices, telemetry

# Create database tables (in-memory SQLite)
models.Base.metadata.create_all(bind=engine)

app = FastAPI(
    docs_url="/api-docs",
    title="IoT Device & Telemetry API",
    description="API for managing IoT Devices and Telemetry (FastAPI Rewrite)",
    version="1.0.0",
    docs_url="/api-docs", 
    redoc_url=None
)

app.include_router(devices.router)
app.include_router(telemetry.router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=3000, reload=True)
