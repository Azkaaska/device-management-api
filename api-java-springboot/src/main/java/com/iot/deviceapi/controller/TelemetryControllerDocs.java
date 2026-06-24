package com.iot.deviceapi.controller;

import com.iot.deviceapi.model.Reading;
import com.iot.deviceapi.model.ReadingInput;
import com.iot.deviceapi.controller.docs.ApiBadRequestError;
import com.iot.deviceapi.controller.docs.ApiNotFoundError;
import com.iot.deviceapi.controller.docs.ApiInternalServerError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/devices")
@Tag(
    name = "Telemetry", 
    description = "Operasi terkait pengiriman dan pengambilan data telemetri deret waktu (time-series)"
)
public interface TelemetryControllerDocs {

    @GetMapping("/{id}/telemetry")
    @Operation(
        summary = "Ambil data telemetri perangkat",
        description = "Mengambil satu data pembacaan telemetri terbaru dari perangkat (jika parameter waktu tidak diberikan), " +
                      "atau daftar riwayat pembacaan telemetri yang terpaginasi dalam rentang waktu start_time dan end_time."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Berhasil mengambil data telemetri. Mengembalikan objek tunggal Reading jika data terbaru dicari, " +
                      "atau array berisi objek Reading jika melakukan pencarian riwayat data.",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(anyOf = { Reading.class })
        )
    )
    @ApiBadRequestError
    @ApiNotFoundError
    @ApiInternalServerError
    Object getTelemetry(
        @Parameter(description = "ID unik perangkat (UUID)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable UUID id,
        
        @Parameter(description = "Timestamp epoch milidetik awal untuk pencarian riwayat", example = "1717488000000")
        @RequestParam(name = "start_time", required = false) Long startTime,
        
        @Parameter(description = "Timestamp epoch milidetik akhir untuk pencarian riwayat", example = "1717574400000")
        @RequestParam(name = "end_time", required = false) Long endTime,
        
        @Parameter(description = "Nomor halaman untuk data riwayat (dimulai dari indeks 0)", example = "0")
        @RequestParam(name = "page", defaultValue = "0") int page,
        
        @Parameter(description = "Jumlah maksimum data riwayat per halaman", example = "20")
        @RequestParam(name = "limit", defaultValue = "20") int limit
    );

    @PostMapping("/{id}/telemetry")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Kirim data telemetri ke perangkat",
        description = "Menyimpan data pembacaan sensor baru (suhu dan kelembapan) untuk perangkat tertentu."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Data telemetri berhasil disimpan",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Reading.class)
        )
    )
    @ApiBadRequestError
    @ApiNotFoundError
    @ApiInternalServerError
    Reading pushTelemetry(
        @Parameter(description = "ID unik perangkat (UUID)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable UUID id,
        
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Data telemetri dari pembacaan sensor perangkat emulator",
            required = true,
            content = @Content(schema = @Schema(implementation = ReadingInput.class))
        )
        @RequestBody ReadingInput input
    );
}
