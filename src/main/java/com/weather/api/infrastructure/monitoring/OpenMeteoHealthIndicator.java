package com.weather.api.infrastructure.monitoring;

import com.weather.api.infrastructure.adapter.client.OpenMeteoFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for Open-Meteo API.
 * Checks if the external API is reachable and responsive.
 *
 * @author Weather API Team
 */
@Slf4j
@Component("openMeteo")
@RequiredArgsConstructor
public class OpenMeteoHealthIndicator implements HealthIndicator {

    private final OpenMeteoFeignClient weatherClient;

    @Override
    public Health health() {
        try {
            long startTime = System.currentTimeMillis();

            // Test with a known good location (New York)
            weatherClient.getWeatherForecast(
                    40.7128,
                    -74.0060,
                    "temperature_2m",
                    "auto"
            );

            long latency = System.currentTimeMillis() - startTime;

            log.debug("Open-Meteo API health check successful, latency: {}ms", latency);

            return Health.up()
                    .withDetail("latency", latency + "ms")
                    .withDetail("service", "Open-Meteo API")
                    .build();

        } catch (Exception e) {
            log.error("Open-Meteo API health check failed: {}", e.getMessage());

            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("service", "Open-Meteo API")
                    .build();
        }
    }
}
