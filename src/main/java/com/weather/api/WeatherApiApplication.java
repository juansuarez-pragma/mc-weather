package com.weather.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main application class for Weather API Service.
 *
 * This service acts as a BFF (Backend For Frontend) for the iOS Weather App,
 * proxying all requests to the Open-Meteo API with added features like:
 * - Caching for improved performance
 * - Circuit breaker for resilience
 * - Rate limiting for security
 * - Structured logging and monitoring
 *
 * @author Weather API Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableFeignClients
@EnableCaching
public class WeatherApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherApiApplication.class, args);
    }
}
