package com.iot.deviceapi.model;

public class WebSocketEvent<T> {
    private String type; // "READING" atau "ALERT"
    private T payload;

    public WebSocketEvent() {}

    public WebSocketEvent(String type, T payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public T getPayload() { return payload; }
    public void setPayload(T payload) { this.payload = payload; }
}
