package com.iot.deviceapi.controller;

import com.iot.deviceapi.model.Device;
import com.iot.deviceapi.model.DeviceInput;
import com.iot.deviceapi.controller.docs.ApiBadRequestError;
import com.iot.deviceapi.controller.docs.ApiNotFoundError;
import com.iot.deviceapi.controller.docs.ApiInternalServerError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/devices")
@Tag(
    name = "Devices", 
    description = "Operasi terkait manajemen atribut dan status perangkat IoT"
)
public interface DeviceControllerDocs {

    @GetMapping
    @Operation(
        summary = "Ambil daftar semua perangkat", 
        description = "Mengambil daftar perangkat IoT dengan sistem paginasi."
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Daftar perangkat berhasil diambil", 
        content = @Content(
            mediaType = "application/json", 
            array = @ArraySchema(schema = @Schema(implementation = Device.class))
        )
    )
    @ApiInternalServerError 
    List<Device> getAllDevices(
        @Parameter(description = "Nomor halaman", example = "0") 
        @RequestParam(name = "page", defaultValue = "0") int page,
        
        @Parameter(description = "Jumlah item per halaman", example = "20") 
        @RequestParam(name = "limit", defaultValue = "20") int limit
    );

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Buat perangkat baru", 
        description = "Mendaftarkan perangkat IoT baru ke dalam sistem."
    )
    @ApiResponse(
        responseCode = "201", 
        description = "Perangkat berhasil dibuat", 
        content = @Content(
            mediaType = "application/json", 
            schema = @Schema(implementation = Device.class)
        )
    )
    @ApiBadRequestError 
    @ApiInternalServerError
    Device createDevice(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Data input perangkat baru", 
            required = true, 
            content = @Content(schema = @Schema(implementation = DeviceInput.class))
        )
        @RequestBody DeviceInput input
    );

    @GetMapping("/{id}")
    @Operation(
        summary = "Ambil detail perangkat berdasarkan ID", 
        description = "Mengambil data metadata lengkap menggunakan UUID."
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Detail perangkat berhasil ditemukan", 
        content = @Content(
            mediaType = "application/json", 
            schema = @Schema(implementation = Device.class)
        )
    )
    @ApiBadRequestError
    @ApiNotFoundError     
    @ApiInternalServerError
    Device getDevice(
        @Parameter(description = "ID unik perangkat", required = true) 
        @PathVariable UUID id
    );

    @PutMapping("/{id}")
    @Operation(
        summary = "Perbarui data perangkat", 
        description = "Memperbarui metadata perangkat yang sudah ada berdasarkan UUID."
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Perangkat berhasil diperbarui", 
        content = @Content(
            mediaType = "application/json", 
            schema = @Schema(implementation = Device.class)
        )
    )
    @ApiBadRequestError
    @ApiNotFoundError
    @ApiInternalServerError
    Device updateDevice(
        @Parameter(description = "ID unik perangkat", required = true) 
        @PathVariable UUID id,
        
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Data pembaruan", 
            required = true, 
            content = @Content(schema = @Schema(implementation = DeviceInput.class))
        )
        @RequestBody DeviceInput input
    );

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Hapus perangkat (Soft Delete)", 
        description = "Mengubah status perangkat menjadi tidak aktif secara logis."
    )
    @ApiResponse(
        responseCode = "204", 
        description = "Perangkat berhasil dinonaktifkan"
    )
    @ApiBadRequestError
    @ApiNotFoundError
    @ApiInternalServerError
    void deleteDevice(
        @Parameter(description = "ID unik perangkat", required = true) 
        @PathVariable UUID id
    );
}
