package com.iot.deviceapi.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Representasi respons error standar dari API")
public class ApiErrorResponse {
    @Schema(description = "Kode status HTTP dari error yang terjadi", example = "404")
    private int status;

    @Schema(description = "Nama kategori/tipe error HTTP", example = "Not Found")
    private String error;

    @Schema(description = "Pesan detail tentang penyebab error", example = "Perangkat dengan ID tersebut tidak ditemukan")
    private String message;

    @Schema(description = "Path URI tempat error terjadi", example = "/api/v1/devices/550e8400-e29b-41d4-a716-446655440000")
    private String path;

    @Schema(description = "Timestamp epoch milidetik saat error terjadi", example = "1717488000000")
    private long timestamp;

    public ApiErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public long getTimestamp() { return timestamp; }
}
