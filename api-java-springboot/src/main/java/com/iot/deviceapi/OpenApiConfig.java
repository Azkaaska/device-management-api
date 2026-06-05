package com.iot.deviceapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "IoT Device & Telemetry API",
        description = "API for managing IoT Devices and Telemetry",
        version = "1.0.0"
    )
)
public class OpenApiConfig {
}
