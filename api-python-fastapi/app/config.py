from typing import Annotated, Any
from pydantic import BeforeValidator
from pydantic_settings import BaseSettings, SettingsConfigDict, NoDecode

# Splits a comma-separated string into a list of cleaned strings
def parse_comma_separated_list(value: Any) -> list[str]:
    if isinstance(value, str):
        return [item.strip() for item in value.split(",")]
    return value

class Settings(BaseSettings):
    # Automatically load environment variables from a .env file
    model_config = SettingsConfigDict(
        env_file=".env", 
        env_file_encoding="utf-8", 
        extra="ignore"
    )

    # App General Settings
    APP_TITLE: str = "IoT Device & Telemetry API"
    VERSION: str = "1.0.0"
    
    # Postgres Configuration
    DATABASE_URL: str = "postgresql://user:password@localhost:5432/database"
    DB_POOL_SIZE: int = 5
    DB_MAX_OVERFLOW: int = 10
    DB_POOL_TIMEOUT: int = 30
    DB_POOL_RECYCLE: int = 1800
    
    # Cassandra Configuration
    # NoDecode prevents Pydantic from forcing strict JSON parsing on the .env string
    # BeforeValidator intercepts the raw string and converts it into a Python list
    CASSANDRA_CONTACT_POINTS: Annotated[
        list[str], 
        NoDecode, 
        BeforeValidator(parse_comma_separated_list)
    ] = ["127.0.0.1"]
    
    CASSANDRA_PORT: int = 9042
    CASSANDRA_KEYSPACE: str = "keyspace"
    CASSANDRA_LOCAL_DC: str = "datacenter"
    CASSANDRA_USER: str = "user"
    CASSANDRA_PASSWORD: str = "password"
    
    # MQTT Configuration
    MQTT_HOST: str = "127.0.0.1"
    MQTT_PORT: int = 1883

settings = Settings()
