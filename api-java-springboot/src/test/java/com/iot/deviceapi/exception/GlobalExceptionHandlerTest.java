package com.iot.deviceapi.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/devices");
    }

    @Test
    void handleResponseStatusException_404_returnsCorrectBody() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Device not found");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/devices");
    }

    @Test
    void handleResponseStatusException_400_returnsCorrectBody() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("name is required");
    }

    @Test
    void handleTypeMismatchException_returns400WithParamName() {
        // MethodArgumentTypeMismatchException perlu reflection untuk dikonstruksi dengan benar
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");

        ResponseEntity<ApiErrorResponse> response = handler.handleTypeMismatchException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("id");
        assertThat(response.getBody().getMessage()).contains("invalid value format");
    }

    @Test
    void handleMalformedJson_returns400WithStandardMessage() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ApiErrorResponse> response = handler.handleMalformedJson(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("Malformed JSON");
    }

    @Test
    void handleGenericException_returns500() {
        RuntimeException ex = new RuntimeException("Unexpected error");

        ResponseEntity<ApiErrorResponse> response = handler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected internal error occurred.");
    }

    @Test
    void apiErrorResponse_timestampIsSetOnConstruction() {
        long before = System.currentTimeMillis();
        ApiErrorResponse error = new ApiErrorResponse(404, "Not Found", "Device not found", "/api/v1/devices/x");
        long after = System.currentTimeMillis();

        assertThat(error.getTimestamp()).isBetween(before, after);
    }
}
