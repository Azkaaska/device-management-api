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
public @interface ApiInternalServerError {}
