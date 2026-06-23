package com.iot.deviceapi.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SensorWebSocketHandler extends TextWebSocketHandler {

    // Thread-safe list to track all active UI browser connections
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("[WEBSOCKET] Live dashboard client connected: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("[WEBSOCKET] Live dashboard client disconnected: " + session.getId());
    }

    private String getDeviceIdQueryParam(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        String query = uri.getQuery();
        if (query == null || query.isEmpty()) return null;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] idx = pair.split("=");
            if (idx.length > 1 && "device_id".equals(idx[0])) {
                return idx[1];
            }
        }
        return null;
    }

    /**
     * Broadcasts enriched sensor telemetry packets to all connected browser sessions.
     */
    public void broadcast(String payload) {
        broadcast(null, payload);
    }

    /**
     * Broadcasts enriched sensor telemetry packets to filtered connected browser sessions.
     */
    public void broadcast(UUID deviceId, String payload) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                String targetDeviceIdStr = getDeviceIdQueryParam(session);
                boolean shouldSend = false;

                if (targetDeviceIdStr == null || targetDeviceIdStr.isEmpty()) {
                    shouldSend = true;
                } else if (deviceId != null) {
                    try {
                        UUID targetDeviceId = UUID.fromString(targetDeviceIdStr);
                        if (targetDeviceId.equals(deviceId)) {
                            shouldSend = true;
                        }
                    } catch (IllegalArgumentException e) {
                        // ignore malformed parameter
                    }
                }

                if (shouldSend) {
                    try {
                        session.sendMessage(new TextMessage(payload));
                    } catch (IOException e) {
                        System.err.println("[WEBSOCKET] Failed to deliver stream packet: " + e.getMessage());
                    }
                }
            }
        }
    }
}
