package com.weather.api.application.mapper;

import com.weather.api.application.dto.response.CurrentWeatherDTO;
import com.weather.api.application.dto.response.WeatherForecastResponse;
import com.weather.api.domain.model.Weather;
import org.springframework.stereotype.Component;

/**
 * Mapper for transforming between Weather domain models and DTOs.
 *
 * @author Weather API Team
 */
@Component
public class WeatherMapper {

    /**
     * Converts Weather domain model to WeatherForecastResponse DTO.
     *
     * @param weather the domain model
     * @return the response DTO
     */
    public WeatherForecastResponse toResponse(Weather weather) {
        if (weather == null) {
            return null;
        }

        return WeatherForecastResponse.builder()
                .latitude(weather.getLatitude())
                .longitude(weather.getLongitude())
                .timezone(weather.getTimezone())
                .current(toCurrentWeatherDTO(weather))
                .build();
    }

    /**
     * Converts Weather domain model to CurrentWeatherDTO.
     *
     * @param weather the domain model
     * @return the current weather DTO
     */
    private CurrentWeatherDTO toCurrentWeatherDTO(Weather weather) {
        if (weather == null) {
            return null;
        }

        return CurrentWeatherDTO.builder()
                .time(weather.getTime())
                .temperature(weather.getTemperature())
                .weatherCode(weather.getWeatherCode())
                .windSpeed(weather.getWindSpeed())
                .humidity(weather.getHumidity())
                .build();
    }
}
