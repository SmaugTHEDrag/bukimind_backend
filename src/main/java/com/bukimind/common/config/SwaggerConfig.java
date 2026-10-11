package com.bukimind.common.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",            // Security scheme name
        scheme = "bearer",              // Bearer token
        type = SecuritySchemeType.HTTP, // HTTP auth
        in = SecuritySchemeIn.HEADER,   // Authorization header
        bearerFormat = "JWT"            // JWT format
)
public class SwaggerConfig {
    // OpenAPI configuration
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // API basic info
                .info(new Info()
                        .title("BukiMind API")
                        .version("1.0.0")
                        .description("API documentation for BukiMind project"))
                // Apply JWT auth globally
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
