package com.weather.api.application.mapper;

import com.weather.api.application.dto.response.CurrentWeatherDTO;
import com.weather.api.application.dto.response.WeatherForecastResponse;
import com.weather.api.domain.model.Weather;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for WeatherMapper.
 *
 * @author Weather API Team
 */
@DisplayName("WeatherMapper Tests")
class WeatherMapperTest {

    private WeatherMapper weatherMapper;

    @BeforeEach
    void setUp() {
        weatherMapper = new WeatherMapper();
    }

    @Test
    @DisplayName("Should map Weather to WeatherForecastResponse correctly")
    void shouldMapWeatherToResponse() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Weather weather = Weather.builder()
                .time(now)
                .temperature(15.5)
                .weatherCode(2)
                .windSpeed(12.3)
                .humidity(65)
                .latitude(40.7128)
                .longitude(-74.0060)
                .timezone("America/New_York")
                .build();

        // When
        WeatherForecastResponse response = weatherMapper.toResponse(weather);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getLatitude()).isEqualTo(40.7128);
        assertThat(response.getLongitude()).isEqualTo(-74.0060);
        assertThat(response.getTimezone()).isEqualTo("America/New_York");

        CurrentWeatherDTO current = response.getCurrent();
        assertThat(current).isNotNull();
        assertThat(current.getTime()).isEqualTo(now);
        assertThat(current.getTemperature()).isEqualTo(15.5);
        assertThat(current.getWeatherCode()).isEqualTo(2);
        assertThat(current.getWindSpeed()).isEqualTo(12.3);
        assertThat(current.getHumidity()).isEqualTo(65);
    }

    @Test
    @DisplayName("Should return null when Weather is null")
    void shouldReturnNullWhenWeatherIsNull() {
        // When
        WeatherForecastResponse response = weatherMapper.toResponse(null);

        // Then
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should handle Weather with null humidity")
    void shouldHandleWeatherWithNullHumidity() {
        // Given
        Weather weather = Weather.builder()
                .time(LocalDateTime.now())
                .temperature(15.5)
                .weatherCode(2)
                .windSpeed(12.3)
                .humidity(null)
                .latitude(40.7128)
                .longitude(-74.0060)
                .timezone("America/New_York")
                .build();

        // When
        WeatherForecastResponse response = weatherMapper.toResponse(weather);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCurrent().getHumidity()).isNull();
    }
}
