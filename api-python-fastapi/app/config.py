from typing import Optional
from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    # App General Settings
    APP_TITLE: str = "IoT Device & Telemetry API"
    VERSION: str = "1.0.0"
    
    # Discord Webhook Notification Configuration
    DISCORD_WEBHOOK_URL: Optional[str] = None
    
    # Postgres configuration
    DATABASE_URL: str = "postgresql://user:password@localhost:5432/database"
    DB_POOL_SIZE: int = 5
    DB_MAX_OVERFLOW: int = 10
    DB_POOL_TIMEOUT: int = 30
    DB_POOL_RECYCLE: int = 1800
    
    # Cassandra Configuration
    CASSANDRA_CONTACT_POINTS: str = "127.0.0.1" 
    CASSANDRA_PORT: int = 9042
    CASSANDRA_KEYSPACE: str = "keyspace"
    CASSANDRA_LOCAL_DC: str = "datacenter"
    CASSANDRA_USER: str = "user"
    CASSANDRA_PASSWORD: str = "password"
    
    # MQTT Configuration
    MQTT_HOST: str = "127.0.0.1"
    MQTT_PORT: int = 1883

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore"
    )

settings = Settings()
