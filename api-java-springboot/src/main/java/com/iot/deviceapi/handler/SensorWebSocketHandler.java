package com.iot.deviceapi.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SensorWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SensorWebSocketHandler.class);

    // CopyOnWriteArrayList digunakan agar iterasi thread-safe: thread MQTT dan HTTP
    // dapat memanggil broadcast() secara bersamaan tanpa perlu mengunci daftar sesi
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("[WEBSOCKET] Live dashboard client connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("[WEBSOCKET] Live dashboard client disconnected: {}", session.getId());
    }

    // Mengirim payload JSON ke semua sesi WebSocket yang sedang terbuka
    public void broadcast(String payload) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(payload));
                } catch (IOException e) {
                    log.error("[WEBSOCKET] Failed to deliver stream packet to session: {}", session.getId(), e);
                }
            }
        }
    }
}
