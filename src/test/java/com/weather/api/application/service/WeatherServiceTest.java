package com.weather.api.application.service;

import com.weather.api.domain.exception.InvalidCoordinatesException;
import com.weather.api.domain.model.GeocodingResult;
import com.weather.api.domain.model.Weather;
import com.weather.api.domain.port.output.WeatherRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for WeatherService.
 *
 * @author Weather API Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherService Tests")
class WeatherServiceTest {

    @Mock
    private WeatherRepositoryPort weatherRepositoryPort;

    @InjectMocks
    private WeatherService weatherService;

    private Weather mockWeather;
    private List<GeocodingResult> mockGeocodingResults;

    @BeforeEach
    void setUp() {
        mockWeather = Weather.builder()
                .time(LocalDateTime.now())
                .temperature(15.5)
                .weatherCode(2)
                .windSpeed(12.3)
                .humidity(65)
                .latitude(40.7128)
                .longitude(-74.0060)
                .timezone("America/New_York")
                .build();

        mockGeocodingResults = Arrays.asList(
                GeocodingResult.builder()
                        .id("1")
                        .name("New York")
                        .latitude(40.7128)
                        .longitude(-74.0060)
                        .country("United States")
                        .admin1("New York")
                        .build(),
                GeocodingResult.builder()
                        .id("2")
                        .name("New York")
                        .latitude(40.7589)
                        .longitude(-73.9851)
                        .country("United States")
                        .admin1("New York")
                        .build()
        );
    }

    @Test
    @DisplayName("Should return weather forecast for valid coordinates")
    void shouldReturnWeatherForecastForValidCoordinates() {
        // Given
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        String timezone = "auto";

        when(weatherRepositoryPort.fetchWeatherForecast(latitude, longitude, timezone))
                .thenReturn(mockWeather);

        // When
        Weather result = weatherService.getWeatherForecast(latitude, longitude, timezone);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLatitude()).isEqualTo(latitude);
        assertThat(result.getLongitude()).isEqualTo(longitude);
        assertThat(result.getTemperature()).isEqualTo(15.5);
        assertThat(result.getWeatherCode()).isEqualTo(2);

        verify(weatherRepositoryPort).fetchWeatherForecast(latitude, longitude, timezone);
    }

    @Test
    @DisplayName("Should use default timezone when null provided")
    void shouldUseDefaultTimezoneWhenNullProvided() {
        // Given
        Double latitude = 40.7128;
        Double longitude = -74.0060;

        when(weatherRepositoryPort.fetchWeatherForecast(anyDouble(), anyDouble(), eq("auto")))
                .thenReturn(mockWeather);

        // When
        Weather result = weatherService.getWeatherForecast(latitude, longitude, null);

        // Then
        assertThat(result).isNotNull();
        verify(weatherRepositoryPort).fetchWeatherForecast(latitude, longitude, "auto");
    }

    @Test
    @DisplayName("Should throw InvalidCoordinatesException for latitude out of range")
    void shouldThrowExceptionForLatitudeOutOfRange() {
        // Given
        Double invalidLatitude = 91.0;
        Double longitude = -74.0060;

        // When & Then
        assertThatThrownBy(() -> weatherService.getWeatherForecast(invalidLatitude, longitude, "auto"))
                .isInstanceOf(InvalidCoordinatesException.class)
                .hasMessageContaining("Invalid coordinates");
    }

    @Test
    @DisplayName("Should throw InvalidCoordinatesException for longitude out of range")
    void shouldThrowExceptionForLongitudeOutOfRange() {
        // Given
        Double latitude = 40.7128;
        Double invalidLongitude = 181.0;

        // When & Then
        assertThatThrownBy(() -> weatherService.getWeatherForecast(latitude, invalidLongitude, "auto"))
                .isInstanceOf(InvalidCoordinatesException.class)
                .hasMessageContaining("Invalid coordinates");
    }

    @Test
    @DisplayName("Should return city search results for valid query")
    void shouldReturnCitySearchResultsForValidQuery() {
        // Given
        String cityName = "New York";
        Integer count = 10;
        String language = "en";

        when(weatherRepositoryPort.searchCity(cityName, count, language))
                .thenReturn(mockGeocodingResults);

        // When
        List<GeocodingResult> results = weatherService.searchCity(cityName, count, language);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("New York");

        verify(weatherRepositoryPort).searchCity(cityName, count, language);
    }

    @Test
    @DisplayName("Should use default values for city search when null provided")
    void shouldUseDefaultValuesForCitySearch() {
        // Given
        String cityName = "New York";

        when(weatherRepositoryPort.searchCity(eq("New York"), eq(10), eq("en")))
                .thenReturn(mockGeocodingResults);

        // When
        List<GeocodingResult> results = weatherService.searchCity(cityName, null, null);

        // Then
        assertThat(results).isNotNull();
        verify(weatherRepositoryPort).searchCity("New York", 10, "en");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for city name too short")
    void shouldThrowExceptionForCityNameTooShort() {
        // Given
        String shortCityName = "N";

        // When & Then
        assertThatThrownBy(() -> weatherService.searchCity(shortCityName, 10, "en"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 2 characters long");
    }

    @Test
    @DisplayName("Should limit count to maximum 20")
    void shouldLimitCountToMaximum20() {
        // Given
        String cityName = "New York";
        Integer excessiveCount = 100;

        when(weatherRepositoryPort.searchCity(anyString(), eq(20), anyString()))
                .thenReturn(mockGeocodingResults);

        // When
        weatherService.searchCity(cityName, excessiveCount, "en");

        // Then
        verify(weatherRepositoryPort).searchCity("New York", 20, "en");
    }

    @Test
    @DisplayName("Should trim city name before searching")
    void shouldTrimCityNameBeforeSearching() {
        // Given
        String cityNameWithSpaces = "  New York  ";

        when(weatherRepositoryPort.searchCity(eq("New York"), anyInt(), anyString()))
                .thenReturn(mockGeocodingResults);

        // When
        weatherService.searchCity(cityNameWithSpaces, 10, "en");

        // Then
        verify(weatherRepositoryPort).searchCity("New York", 10, "en");
    }
}
