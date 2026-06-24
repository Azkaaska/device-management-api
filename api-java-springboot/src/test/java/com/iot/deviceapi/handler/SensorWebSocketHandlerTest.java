package com.iot.deviceapi.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static org.mockito.Mockito.*;

class SensorWebSocketHandlerTest {

    private SensorWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SensorWebSocketHandler();
    }

    private WebSocketSession openSession(String id) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(id);
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    @Test
    void afterConnectionEstablished_sessionAddedAndReceivesBroadcast() throws IOException {
        WebSocketSession session = openSession("s1");

        handler.afterConnectionEstablished(session);
        handler.broadcast("hello");

        verify(session).sendMessage(new TextMessage("hello"));
    }

    @Test
    void afterConnectionClosed_sessionRemovedAndNoLongerReceivesBroadcast() throws IOException {
        WebSocketSession session = openSession("s2");

        handler.afterConnectionEstablished(session);
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        handler.broadcast("hello");

        verify(session, never()).sendMessage(any());
    }

    @Test
    void broadcast_multipleSessions_allReceivePayload() throws IOException {
        WebSocketSession s1 = openSession("s3");
        WebSocketSession s2 = openSession("s4");

        handler.afterConnectionEstablished(s1);
        handler.afterConnectionEstablished(s2);
        handler.broadcast("data");

        verify(s1).sendMessage(new TextMessage("data"));
        verify(s2).sendMessage(new TextMessage("data"));
    }

    @Test
    void broadcast_closedSession_skippedWithoutError() throws IOException {
        WebSocketSession closedSession = mock(WebSocketSession.class);
        when(closedSession.getId()).thenReturn("s5");
        when(closedSession.isOpen()).thenReturn(false);

        handler.afterConnectionEstablished(closedSession);
        handler.broadcast("data");

        // sendMessage tidak boleh dipanggil pada sesi yang sudah tertutup
        verify(closedSession, never()).sendMessage(any());
    }

    @Test
    void broadcast_ioExceptionOnOneSession_doesNotAbortOtherSessions() throws IOException {
        WebSocketSession failing = openSession("s6");
        WebSocketSession healthy = openSession("s7");

        doThrow(new IOException("broken pipe")).when(failing).sendMessage(any());

        handler.afterConnectionEstablished(failing);
        handler.afterConnectionEstablished(healthy);
        handler.broadcast("data");

        // Sesi yang sehat tetap harus menerima payload meskipun ada kegagalan sebelumnya
        verify(healthy).sendMessage(new TextMessage("data"));
    }

    @Test
    void broadcast_noSessions_doesNotThrow() {
        // Harus beroperasi tanpa error saat tidak ada klien yang terhubung
        handler.broadcast("data");
    }
}
