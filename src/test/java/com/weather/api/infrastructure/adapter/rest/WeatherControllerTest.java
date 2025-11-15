package com.weather.api.infrastructure.adapter.rest;

import com.weather.api.application.mapper.GeocodingMapper;
import com.weather.api.application.mapper.WeatherMapper;
import com.weather.api.domain.exception.CityNotFoundException;
import com.weather.api.domain.exception.ExternalApiException;
import com.weather.api.domain.exception.InvalidCoordinatesException;
import com.weather.api.domain.model.GeocodingResult;
import com.weather.api.domain.model.Weather;
import com.weather.api.domain.port.input.WeatherUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for WeatherController using MockMvc.
 *
 * @author Weather API Team
 */
@WebMvcTest(WeatherController.class)
@DisplayName("WeatherController Tests")
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherUseCase weatherUseCase;

    @MockBean
    private WeatherMapper weatherMapper;

    @MockBean
    private GeocodingMapper geocodingMapper;

    @Test
    @DisplayName("GET /api/v1/weather/forecast should return 200 with valid coordinates")
    void shouldReturnWeatherForecastWithValidCoordinates() throws Exception {
        // Given
        Double latitude = 40.7128;
        Double longitude = -74.0060;

        Weather mockWeather = Weather.builder()
                .time(LocalDateTime.now())
                .temperature(15.5)
                .weatherCode(2)
                .windSpeed(12.3)
                .humidity(65)
                .latitude(latitude)
                .longitude(longitude)
                .timezone("America/New_York")
                .build();

        when(weatherUseCase.getWeatherForecast(eq(latitude), eq(longitude), anyString()))
                .thenReturn(mockWeather);

        // Note: In real test, we'd also mock weatherMapper.toResponse()
        // For simplicity, we're testing the controller integration

        // When & Then
        mockMvc.perform(get("/api/v1/weather/forecast")
                        .param("latitude", latitude.toString())
                        .param("longitude", longitude.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/weather/forecast should return 400 for latitude out of range")
    void shouldReturn400ForLatitudeOutOfRange() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/weather/forecast")
                        .param("latitude", "91.0")
                        .param("longitude", "-74.0060"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/weather/forecast should return 400 for longitude out of range")
    void shouldReturn400ForLongitudeOutOfRange() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/weather/forecast")
                        .param("latitude", "40.7128")
                        .param("longitude", "181.0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/weather/forecast should return 400 for missing latitude")
    void shouldReturn400ForMissingLatitude() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/weather/forecast")
                        .param("longitude", "-74.0060"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/weather/forecast should return 400 for missing longitude")
    void shouldReturn400ForMissingLongitude() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/weather/forecast")
                        .param("latitude", "40.7128"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/weather/forecast should return 400 when service throws InvalidCoordinatesException")
    void shouldReturn400WhenInvalidCoordinatesException() throws Exception {
        // Given
        when(weatherUseCase.getWeatherForecast(eq(40.7128), eq(-74.0060), anyString()))
                .thenThrow(new InvalidCoordinatesException(40.7128, -74.0060));

        // When & Then
        mockMvc.perform(get("/api/v1/weather/forecast")
                        .param("latitude", "40.7128")
                        .param("longitude", "-74.0060"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/weather/forecast should return 503 when service throws ExternalApiException")
    void shouldReturn503WhenExternalApiException() throws Exception {
        // Given
        when(weatherUseCase.getWeatherForecast(eq(40.7128), eq(-74.0060), anyString()))
                .thenThrow(new ExternalApiException("External API is down"));

        // When & Then
        mockMvc.perform(get("/api/v1/weather/forecast")
                        .param("latitude", "40.7128")
                        .param("longitude", "-74.0060"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("GET /api/v1/weather/search should return 200 with valid city name")
    void shouldReturnCitySearchResultsWithValidName() throws Exception {
        // Given
        String cityName = "New York";
        List<GeocodingResult> mockResults = Arrays.asList(
                GeocodingResult.builder()
                        .id("1")
                        .name("New York")
                        .latitude(40.7128)
                        .longitude(-74.0060)
                        .country("United States")
                        .admin1("New York")
                        .build()
        );

        when(weatherUseCase.searchCity(eq(cityName), anyInt(), anyString()))
                .thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/api/v1/weather/search")
                        .param("name", cityName))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/weather/search should return 400 for city name too short")
    void shouldReturn400ForCityNameTooShort() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/weather/search")
                        .param("name", "N"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/weather/search should return 400 for missing city name")
    void shouldReturn400ForMissingCityName() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/weather/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/weather/search should return 404 when city not found")
    void shouldReturn404WhenCityNotFound() throws Exception {
        // Given
        when(weatherUseCase.searchCity(eq("NonexistentCity"), anyInt(), anyString()))
                .thenThrow(new CityNotFoundException("NonexistentCity"));

        // When & Then
        mockMvc.perform(get("/api/v1/weather/search")
                        .param("name", "NonexistentCity"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/weather/search should return 400 for count exceeding maximum")
    void shouldReturn400ForCountExceedingMaximum() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/weather/search")
                        .param("name", "New York")
                        .param("count", "21"))
                .andExpect(status().isBadRequest());
    }
}
