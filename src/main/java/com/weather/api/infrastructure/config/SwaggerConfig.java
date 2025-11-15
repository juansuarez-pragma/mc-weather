package com.weather.api.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration for API documentation.
 *
 * @author Weather API Team
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI weatherApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Weather API Service")
                        .description("""
                                Backend service for iOS Weather App.

                                This API acts as a BFF (Backend For Frontend) proxying requests to Open-Meteo API with:
                                - Intelligent caching for improved performance
                                - Circuit breaker for resilience
                                - Rate limiting for security
                                - Comprehensive monitoring and logging

                                ## Features
                                - Get current weather by coordinates
                                - Search cities by name
                                - Automatic timezone detection
                                - WMO weather codes support
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Weather API Team")
                                .email("weather-api@example.com")
                                .url("https://github.com/your-org/weather-api"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.weather-app.com")
                                .description("Production server")
                ));
    }
}
