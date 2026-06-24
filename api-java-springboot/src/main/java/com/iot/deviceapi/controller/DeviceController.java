package com.iot.deviceapi.controller;

import com.iot.deviceapi.model.Device;
import com.iot.deviceapi.model.DeviceInput;
import com.iot.deviceapi.exception.ApiErrorResponse;
import com.iot.deviceapi.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@Tag(name = "Devices", description = "Operasi terkait manajemen atribut dan status perangkat IoT")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    @Operation(
        summary = "Ambil daftar semua perangkat",
        description = "Mengambil daftar perangkat IoT yang terdaftar dengan sistem paginasi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Daftar perangkat berhasil diambil",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = Device.class))
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Kesalahan server internal",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "500 Internal Server Error",
                    value = "{\"status\": 500, \"error\": \"Internal Server Error\", \"message\": \"Terjadi kesalahan internal pada server\", \"path\": \"/api/v1/devices\", \"timestamp\": 1717488000000}"
                )
            )
        )
    })
    public List<Device> getAllDevices(
            @Parameter(description = "Nomor halaman (dimulai dari indeks 0)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Jumlah maksimum item per halaman", example = "20")
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        return deviceService.getAllDevices(page, limit);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Buat perangkat baru",
        description = "Mendaftarkan perangkat IoT baru ke dalam sistem."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Perangkat berhasil dibuat",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Device.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Struktur atau data permintaan tidak valid",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "400 Bad Request",
                    value = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"Format JSON tidak valid atau data wajib tidak lengkap\", \"path\": \"/api/v1/devices\", \"timestamp\": 1717488000000}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Kesalahan server internal",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "500 Internal Server Error",
                    value = "{\"status\": 500, \"error\": \"Internal Server Error\", \"message\": \"Terjadi kesalahan internal pada server\", \"path\": \"/api/v1/devices\", \"timestamp\": 1717488000000}"
                )
            )
        )
    })
    public Device createDevice(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Data input perangkat baru yang akan dibuat",
                required = true,
                content = @Content(schema = @Schema(implementation = DeviceInput.class))
            )
            @RequestBody DeviceInput input) {
        return deviceService.createDevice(input);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Ambil detail perangkat berdasarkan ID",
        description = "Mengambil data metadata lengkap perangkat IoT menggunakan UUID perangkat."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Detail perangkat berhasil ditemukan",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Device.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Format ID (UUID) tidak valid",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "400 Bad Request",
                    value = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"Parameter 'id' memiliki format UUID yang salah\", \"path\": \"/api/v1/devices/invalid-uuid\", \"timestamp\": 1717488000000}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Perangkat tidak ditemukan",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "404 Not Found",
                    value = "{\"status\": 404, \"error\": \"Not Found\", \"message\": \"Perangkat tidak ditemukan\", \"path\": \"/api/v1/devices/550e8400-e29b-41d4-a716-446655440000\", \"timestamp\": 1717488000000}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Kesalahan server internal",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "500 Internal Server Error",
                    value = "{\"status\": 500, \"error\": \"Internal Server Error\", \"message\": \"Terjadi kesalahan internal pada server\", \"path\": \"/api/v1/devices/550e8400-e29b-41d4-a716-446655440000\", \"timestamp\": 1717488000000}"
                )
            )
        )
    })
    public Device getDevice(
            @Parameter(description = "ID unik perangkat (UUID)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        return deviceService.getDeviceById(id);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Perbarui data perangkat",
        description = "Memperbarui metadata perangkat yang sudah ada berdasarkan UUID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Perangkat berhasil diperbarui",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Device.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Data pembaruan tidak valid atau format ID salah",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "400 Bad Request",
                    value = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"Format data tidak valid atau format UUID tidak sesuai\", \"path\": \"/api/v1/devices/550e8400-e29b-41d4-a716-446655440000\", \"timestamp\": 1717488000000}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Perangkat tidak ditemukan",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "404 Not Found",
                    value = "{\"status\": 404, \"error\": \"Not Found\", \"message\": \"Perangkat tidak ditemukan\", \"path\": \"/api/v1/devices/550e8400-e29b-41d4-a716-446655440000\", \"timestamp\": 1717488000000}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Kesalahan server internal",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "500 Internal Server Error",
                    value = "{\"status\": 500, \"error\": \"Internal Server Error\", \"message\": \"Terjadi kesalahan internal pada server\", \"path\": \"/api/v1/devices/550e8400-e29b-41d4-a716-446655440000\", \"timestamp\": 1717488000000}"
                )
            )
        )
    })
    public Device updateDevice(
            @Parameter(description = "ID unik perangkat (UUID) yang akan diperbarui", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Data pembaruan metadata perangkat",
                required = true,
                content = @Content(schema = @Schema(implementation = DeviceInput.class))
            )
            @RequestBody DeviceInput input) {
        return deviceService.updateDevice(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Hapus perangkat (Soft Delete)",
        description = "Mengubah status perangkat menjadi tidak aktif secara logis (soft delete) tanpa menghapus baris data dari database."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Perangkat berhasil dinonaktifkan (tanpa body respons)"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Format ID (UUID) tidak valid",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "400 Bad Request",
                    value = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"Parameter 'id' memiliki format UUID yang salah\", \"path\": \"/api/v1/devices/invalid-uuid\", \"timestamp\": 1717488000000}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Perangkat tidak ditemukan",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "404 Not Found",
                    value = "{\"status\": 404, \"error\": \"Not Found\", \"message\": \"Perangkat tidak ditemukan\", \"path\": \"/api/v1/devices/550e8400-e29b-41d4-a716-446655440000\", \"timestamp\": 1717488000000}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Kesalahan server internal",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "500 Internal Server Error",
                    value = "{\"status\": 500, \"error\": \"Internal Server Error\", \"message\": \"Terjadi kesalahan internal pada server\", \"path\": \"/api/v1/devices/550e8400-e29b-41d4-a716-446655440000\", \"timestamp\": 1717488000000}"
                )
            )
        )
    })
    public void deleteDevice(
            @Parameter(description = "ID unik perangkat (UUID) yang akan dinonaktifkan", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        deviceService.softDeleteDevice(id);
    }
}
