const { WebSocketServer } = require('ws');
const eventHub = require('../utils/eventHub');

function initWebSocketServer(server) {
    const wss = new WebSocketServer({ server, path: '/api/ws' });

    wss.on('connection', (ws, req) => {
        // Resolve target device query filtering out of the request URL string
        const url = new URL(req.url, `http://${req.headers.host}`);
        const targetDeviceId = url.searchParams.get('device_id');

        console.log(`[WebSocket Connected]: Stream registered for Device ID: ${targetDeviceId || 'ALL'}`);

        // Define listener callback function
        const broadcastHandler = (telemetry) => {
            // Route data if client subscribed to all or if device matches the specific filter
            if (!targetDeviceId || telemetry.device_id === targetDeviceId) {
                if (ws.readyState === ws.OPEN) {
                    ws.send(JSON.stringify(telemetry));
                }
            }
        };

        // Attach listener to central event bus
        eventHub.on('telemetry:new', broadcastHandler);

        ws.on('close', () => {
            console.log(`[WebSocket Disconnected]: Removing listener for Device ID: ${targetDeviceId || 'ALL'}`);
            eventHub.removeListener('telemetry:new', broadcastHandler);
        });

        ws.on('error', (err) => {
            console.error(`[WebSocket Error]: Socket failure: ${err.message}`);
            eventHub.removeListener('telemetry:new', broadcastHandler);
        });
    });

    console.log('WebSocket Server mounted successfully on route /api/ws');
}

module.exports = { initWebSocketServer };
