package com.iot.deviceapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.UUID;

import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
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
    
    static {
        StringSchema uuidSchema = new StringSchema();
        uuidSchema.setFormat("uuid");
        uuidSchema.setExample("550e8400-e29b-41d4-a716-446655440000");
        
        SpringDocUtils.getConfig().replaceWithSchema(UUID.class, uuidSchema);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI(); // Skema global sudah ditangani oleh blok static di atas
    }
}
