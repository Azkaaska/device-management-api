# IoT Device Management API

This repository contains the backend implementation of the IoT Device Management API, written in three different languages/frameworks to demonstrate identical architecture, routing, and behavior across distinct technology stacks.

## Implementations

1. **Node.js (Express)** - Located in `/api-nodejs-express`
2. **Python (FastAPI)** - Located in `/api-python-fastapi`
3. **Java (Spring Boot)** - Located in `/api-java-springboot`

## API Architecture

All three APIs share the exact same database schema and routing conventions:

*   **Database**: In-memory databases are used across all three (SQLite for Node/Python, H2 for Java).
*   **Device IDs**: Natively generated string UUIDs (`UUIDv4`).
*   **Timestamps**: Stored and returned as integer UNIX timestamps in milliseconds.
*   **Port**: All servers default to listening on `http://localhost:3000`.

### Endpoints
*   `POST /api/devices` - Create a device
*   `GET /api/devices` - Get all devices
*   `GET /api/devices/{id}` - Get a specific device
*   `PUT /api/devices/{id}` - Update a device
*   `DELETE /api/devices/{id}` - Delete a device
*   `POST /api/devices/{id}/telemetry` - Push telemetry data for a device
*   `GET /api/devices/{id}/telemetry` - Get the latest telemetry data point for a device

## Testing & Documentation
A unified `postman_collection.json` is provided in the root directory. You can import this into Postman to test any of the three servers immediately (it handles UUID injection dynamically).

All three APIs also expose interactive Swagger/OpenAPI documentation at: `http://localhost:3000/api-docs`
