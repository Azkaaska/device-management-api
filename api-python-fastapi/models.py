from sqlalchemy import Column, Integer, String, Float, ForeignKey, BigInteger
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship
import time
import uuid

from database import Base

def get_current_unix():
    return int(time.time() * 1000)

class Device(Base):
    __tablename__ = "devices"

    id = Column(UUID(as_uuid=True), primary_key=True, index=True, default=lambda: uuid.uuid4())
    name = Column(String, index=True, nullable=False)
    type = Column(String, nullable=True)
    status = Column(String, default="ACTIVE")
    created_at = Column(BigInteger, default=get_current_unix)

    telemetries = relationship("Telemetry", back_populates="device", cascade="all, delete-orphan")


class Telemetry(Base):
    __tablename__ = "telemetry"

    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(UUID(as_uuid=True), ForeignKey("devices.id", ondelete="CASCADE"), nullable=False)
    temperature = Column(Float)
    humidity = Column(Float)
    ts = Column(BigInteger, default=get_current_unix)

    device = relationship("Device", back_populates="telemetries")
