from sqlalchemy import Column, String, Index
from sqlalchemy.dialects.postgresql import UUID
import uuid
from app.core.postgres_db import Base

class Device(Base):
    __tablename__ = "devices"

    id = Column(UUID(as_uuid=True), primary_key=True, default=lambda: uuid.uuid4())
    name = Column(String(100), nullable=False)
    type = Column(String(50), nullable=False)
    status = Column(String(20), nullable=False, default="active")

    __table_args__ = (
        Index("idx_devices_type", "type"),
        Index("idx_devices_status_type", "status", "type"),
    )
