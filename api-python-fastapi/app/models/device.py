from sqlalchemy import Column, String, BigInteger, Index
from sqlalchemy.dialects.postgresql import UUID, JSONB
import time
import uuid
from app.core.postgres_db import Base

def get_current_unix_ms():
    return time.time_ns() // 1000000

class Device(Base):
    __tablename__ = "devices"

    device_id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    device_name = Column(String(100), nullable=False)
    device_type = Column(String(50), nullable=False)
    status = Column(String(20), nullable=False, default="ACTIVE")
    firmware_version = Column(String(20), nullable=True)
    device_metadata = Column(JSONB, nullable=True)
    created_at = Column(BigInteger, nullable=False, default=get_current_unix_ms)
    updated_at = Column(BigInteger, nullable=False, default=get_current_unix_ms, onupdate=get_current_unix_ms)

    __table_args__ = (
        Index("idx_devices_type", "device_type"),
        Index("idx_devices_status_type", "status", "device_type"),
    )
