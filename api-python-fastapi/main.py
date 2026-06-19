from contextlib import asynccontextmanager
from fastapi import FastAPI

from app.config import settings
from app.core.postgres_db import engine, Base
from app.core.cassandra_db import cassandra_db
from app.core.mqtt_worker import mqtt_worker
from app.models.reading import Reading
from app.api.router import api_router

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Setup PostgreSQL Schemas
    Base.metadata.create_all(bind=engine)
    
    # Secure Cassandra session context and configure structures
    cassandra_db.connect()
    Reading.init_table()
    
    # Initialize background safe non-blocking MQTT loop threads
    mqtt_worker.start()
    
    yield
    
    # Tear down services upon worker shutdown gracefully
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

app.include_router(api_router, prefix="/api/v1")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=3000, reload=True)
