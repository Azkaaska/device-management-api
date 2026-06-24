package com.iot.deviceapi.controller.docs;

import com.iot.deviceapi.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
    responseCode = "404",
    description = "Perangkat tidak ditemukan",
    content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ApiErrorResponse.class),
        examples = @ExampleObject(
            name = "404 Not Found", 
            value = "{\"status\": 404, \"error\": \"Not Found\", \"message\": \"Perangkat tidak ditemukan\", \"path\": \"/api/v1/devices\", \"timestamp\": 1717488000000}"
        )
    )
)
public @interface ApiNotFoundError {}
