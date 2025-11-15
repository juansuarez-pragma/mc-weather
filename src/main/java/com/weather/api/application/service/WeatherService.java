package com.weather.api.application.service;

import com.weather.api.domain.exception.InvalidCoordinatesException;
import com.weather.api.domain.model.GeocodingResult;
import com.weather.api.domain.model.Location;
import com.weather.api.domain.model.Weather;
import com.weather.api.domain.port.input.WeatherUseCase;
import com.weather.api.domain.port.output.WeatherRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for weather operations.
 * This class implements business logic and orchestrates calls to external repositories.
 *
 * @author Weather API Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService implements WeatherUseCase {

    private final WeatherRepositoryPort weatherRepositoryPort;

    @Override
    @Cacheable(
            value = "weatherForecast",
            key = "#latitude + '_' + #longitude",
            unless = "#result == null"
    )
    public Weather getWeatherForecast(Double latitude, Double longitude, String timezone) {
        log.info("Getting weather forecast for coordinates: ({}, {})", latitude, longitude);

        // Add to MDC for tracing
        MDC.put("latitude", String.valueOf(latitude));
        MDC.put("longitude", String.valueOf(longitude));

        try {
            // Validate coordinates
            validateCoordinates(latitude, longitude);

            // Set default timezone if not provided
            String tz = (timezone == null || timezone.isEmpty()) ? "auto" : timezone;

            // Fetch weather from repository
            Weather weather = weatherRepositoryPort.fetchWeatherForecast(latitude, longitude, tz);

            log.info("Weather forecast retrieved successfully: temp={}°C, code={}",
                    weather.getTemperature(), weather.getWeatherCode());

            return weather;
        } finally {
            MDC.clear();
        }
    }

    @Override
    @Cacheable(
            value = "citySearch",
            key = "#cityName + '_' + #count + '_' + #language",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<GeocodingResult> searchCity(String cityName, Integer count, String language) {
        log.info("Searching for city: '{}' with count={}, language={}", cityName, count, language);

        MDC.put("cityName", cityName);

        try {
            // Validate input
            if (cityName == null || cityName.trim().length() < 2) {
                throw new IllegalArgumentException("City name must be at least 2 characters long");
            }

            // Set defaults
            int resultCount = (count == null || count < 1) ? 10 : Math.min(count, 20);
            String lang = (language == null || language.isEmpty()) ? "en" : language;

            // Search city
            List<GeocodingResult> results = weatherRepositoryPort.searchCity(
                    cityName.trim(),
                    resultCount,
                    lang
            );

            log.info("Found {} results for city: '{}'", results.size(), cityName);

            return results;
        } finally {
            MDC.clear();
        }
    }

    /**
     * Validates geographical coordinates.
     *
     * @param latitude  the latitude
     * @param longitude the longitude
     * @throws InvalidCoordinatesException if coordinates are invalid
     */
    private void validateCoordinates(Double latitude, Double longitude) {
        Location location = Location.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();

        if (!location.isValid()) {
            log.error("Invalid coordinates: lat={}, lon={}", latitude, longitude);
            throw new InvalidCoordinatesException(latitude, longitude);
        }
    }
}
