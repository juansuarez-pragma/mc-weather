package com.weather.api.infrastructure.adapter.client;

import com.weather.api.domain.exception.CityNotFoundException;
import com.weather.api.domain.exception.ExternalApiException;
import com.weather.api.domain.model.GeocodingResult;
import com.weather.api.domain.model.Weather;
import com.weather.api.domain.port.output.WeatherRepositoryPort;
import com.weather.api.infrastructure.adapter.client.dto.OpenMeteoGeocodingResponse;
import com.weather.api.infrastructure.adapter.client.dto.OpenMeteoWeatherResponse;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of WeatherRepositoryPort using Open-Meteo API via Feign.
 * This class is an adapter in the hexagonal architecture.
 *
 * @author Weather API Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenMeteoClientImpl implements WeatherRepositoryPort {

    private static final String CURRENT_PARAMS = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m";
    private static final String CIRCUIT_BREAKER_NAME = "openMeteoService";

    private final OpenMeteoFeignClient weatherClient;
    private final OpenMeteoGeocodingFeignClient geocodingClient;

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fetchWeatherForecastFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public Weather fetchWeatherForecast(Double latitude, Double longitude, String timezone) {
        log.debug("Calling Open-Meteo API for weather forecast: lat={}, lon={}", latitude, longitude);

        try {
            OpenMeteoWeatherResponse response = weatherClient.getWeatherForecast(
                    latitude,
                    longitude,
                    CURRENT_PARAMS,
                    timezone
            );

            return mapToWeather(response);

        } catch (FeignException.NotFound e) {
            log.error("Weather data not found for coordinates: ({}, {})", latitude, longitude);
            throw new ExternalApiException("Weather data not found for the specified location", 404);

        } catch (FeignException.ServiceUnavailable | FeignException.BadGateway e) {
            log.error("Open-Meteo API is unavailable: {}", e.getMessage());
            throw new ExternalApiException("Weather service is temporarily unavailable", e);

        } catch (FeignException e) {
            log.error("Error calling Open-Meteo API: status={}, message={}",
                    e.status(), e.getMessage());
            throw new ExternalApiException(
                    "Failed to fetch weather data: " + e.getMessage(),
                    e.status()
            );

        } catch (Exception e) {
            log.error("Unexpected error fetching weather: {}", e.getMessage(), e);
            throw new ExternalApiException("Unexpected error fetching weather data", e);
        }
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "searchCityFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public List<GeocodingResult> searchCity(String cityName, Integer count, String language) {
        log.debug("Calling Open-Meteo Geocoding API for city: '{}', count={}", cityName, count);

        try {
            OpenMeteoGeocodingResponse response = geocodingClient.searchCity(
                    cityName,
                    count,
                    language,
                    "json"
            );

            if (response.getResults() == null || response.getResults().isEmpty()) {
                log.warn("No results found for city: '{}'", cityName);
                throw new CityNotFoundException(cityName);
            }

            return response.getResults().stream()
                    .map(this::mapToGeocodingResult)
                    .collect(Collectors.toList());

        } catch (CityNotFoundException e) {
            throw e;

        } catch (FeignException.NotFound e) {
            log.error("City not found: '{}'", cityName);
            throw new CityNotFoundException(cityName);

        } catch (FeignException.ServiceUnavailable | FeignException.BadGateway e) {
            log.error("Open-Meteo Geocoding API is unavailable: {}", e.getMessage());
            throw new ExternalApiException("Geocoding service is temporarily unavailable", e);

        } catch (FeignException e) {
            log.error("Error calling Open-Meteo Geocoding API: status={}, message={}",
                    e.status(), e.getMessage());
            throw new ExternalApiException(
                    "Failed to search city: " + e.getMessage(),
                    e.status()
            );

        } catch (Exception e) {
            log.error("Unexpected error searching city: {}", e.getMessage(), e);
            throw new ExternalApiException("Unexpected error searching city", e);
        }
    }

    /**
     * Fallback method for weather forecast when circuit breaker is open.
     */
    private Weather fetchWeatherForecastFallback(Double latitude, Double longitude, String timezone, Exception e) {
        log.error("Fallback activated for weather forecast: {}", e.getMessage());
        throw new ExternalApiException(
                "Weather service is currently unavailable. Please try again later.",
                e
        );
    }

    /**
     * Fallback method for city search when circuit breaker is open.
     */
    private List<GeocodingResult> searchCityFallback(String cityName, Integer count, String language, Exception e) {
        log.error("Fallback activated for city search: {}", e.getMessage());
        throw new ExternalApiException(
                "City search service is currently unavailable. Please try again later.",
                e
        );
    }

    /**
     * Maps Open-Meteo weather response to domain model.
     */
    private Weather mapToWeather(OpenMeteoWeatherResponse response) {
        OpenMeteoWeatherResponse.CurrentData current = response.getCurrent();

        LocalDateTime time = LocalDateTime.parse(
                current.getTime(),
                DateTimeFormatter.ISO_DATE_TIME
        );

        return Weather.builder()
                .time(time)
                .temperature(current.getTemperature())
                .weatherCode(current.getWeatherCode())
                .windSpeed(current.getWindSpeed())
                .humidity(current.getHumidity())
                .latitude(response.getLatitude())
                .longitude(response.getLongitude())
                .timezone(response.getTimezone())
                .build();
    }

    /**
     * Maps Open-Meteo geocoding result to domain model.
     */
    private GeocodingResult mapToGeocodingResult(OpenMeteoGeocodingResponse.GeocodingResult result) {
        return GeocodingResult.builder()
                .id(UUID.randomUUID().toString()) // Generate UUID for each result
                .name(result.getName())
                .latitude(result.getLatitude())
                .longitude(result.getLongitude())
                .country(result.getCountry())
                .admin1(result.getAdmin1())
                .build();
    }
}
