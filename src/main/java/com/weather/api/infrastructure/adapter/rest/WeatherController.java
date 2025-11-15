package com.weather.api.infrastructure.adapter.rest;

import com.weather.api.application.dto.request.CitySearchRequest;
import com.weather.api.application.dto.request.WeatherForecastRequest;
import com.weather.api.application.dto.response.CitySearchResponse;
import com.weather.api.application.dto.response.WeatherForecastResponse;
import com.weather.api.application.mapper.GeocodingMapper;
import com.weather.api.application.mapper.WeatherMapper;
import com.weather.api.domain.model.GeocodingResult;
import com.weather.api.domain.model.Weather;
import com.weather.api.domain.port.input.WeatherUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for weather API endpoints.
 * Handles HTTP requests and delegates to business logic.
 *
 * @author Weather API Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Validated
@Tag(name = "Weather API", description = "Endpoints for weather forecast and city search")
public class WeatherController {

    private final WeatherUseCase weatherUseCase;
    private final WeatherMapper weatherMapper;
    private final GeocodingMapper geocodingMapper;

    @Operation(
            summary = "Get weather forecast by coordinates",
            description = "Returns current weather conditions for specified geographical coordinates"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = WeatherForecastResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid coordinates or parameters"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - rate limit exceeded"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "External API unavailable"
            )
    })
    @GetMapping("/forecast")
    public ResponseEntity<WeatherForecastResponse> getWeatherForecast(
            @RequestParam
            @NotNull(message = "Latitude is required")
            @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
            @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
            Double latitude,

            @RequestParam
            @NotNull(message = "Longitude is required")
            @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
            @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
            Double longitude,

            @RequestParam(required = false, defaultValue = "auto")
            String timezone
    ) {
        log.info("GET /api/v1/weather/forecast - lat: {}, lon: {}, timezone: {}",
                latitude, longitude, timezone);

        Weather weather = weatherUseCase.getWeatherForecast(latitude, longitude, timezone);
        WeatherForecastResponse response = weatherMapper.toResponse(weather);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Search for cities by name",
            description = "Returns a list of cities matching the search query with their coordinates"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = CitySearchResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search parameters (name too short, count out of range)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No cities found matching the search query"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - rate limit exceeded"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "External API unavailable"
            )
    })
    @GetMapping("/search")
    public ResponseEntity<CitySearchResponse> searchCity(
            @RequestParam
            @NotBlank(message = "City name is required")
            @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters")
            String name,

            @RequestParam(required = false, defaultValue = "10")
            @Min(value = 1, message = "Count must be at least 1")
            @Max(value = 20, message = "Count must not exceed 20")
            Integer count,

            @RequestParam(required = false, defaultValue = "en")
            String language
    ) {
        log.info("GET /api/v1/weather/search - name: '{}', count: {}, language: {}",
                name, count, language);

        List<GeocodingResult> results = weatherUseCase.searchCity(name, count, language);
        CitySearchResponse response = geocodingMapper.toResponse(results);

        return ResponseEntity.ok(response);
    }
}
