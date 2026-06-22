import asyncio
from fastapi import WebSocket
from uuid import UUID
from typing import Dict, List, Optional

class ConnectionManager:
    def __init__(self):
        self.active_connections: Dict[Optional[UUID], List[WebSocket]] = {}
        self.loop = None

    def set_loop(self, loop: asyncio.AbstractEventLoop):
        self.loop = loop

    async def connect(self, device_id: Optional[UUID], websocket: WebSocket):
        await websocket.accept()
        if device_id not in self.active_connections:
            self.active_connections[device_id] = []
        self.active_connections[device_id].append(websocket)

    def disconnect(self, device_id: Optional[UUID], websocket: WebSocket):
        if device_id in self.active_connections:
            try:
                self.active_connections[device_id].remove(websocket)
                if not self.active_connections[device_id]:
                    del self.active_connections[device_id]
            except ValueError:
                pass

    async def broadcast(self, device_id: UUID, message: dict):
        # 1. Broadcast to device-specific subscribers
        if device_id in self.active_connections:
            for connection in self.active_connections[device_id]:
                try:
                    await connection.send_json(message)
                except Exception:
                    pass
        # 2. Broadcast to global (ALL devices) subscribers
        if None in self.active_connections:
            for connection in self.active_connections[None]:
                try:
                    await connection.send_json(message)
                except Exception:
                    pass

    def broadcast_sync(self, device_id: UUID, message: dict):
        if self.loop:
            asyncio.run_coroutine_threadsafe(self.broadcast(device_id, message), self.loop)

manager = ConnectionManager()
