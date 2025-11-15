package com.weather.api.domain.port.output;

import com.weather.api.domain.model.GeocodingResult;
import com.weather.api.domain.model.Weather;

import java.util.List;

/**
 * Output port (repository interface) for weather data operations.
 * This interface defines how to retrieve weather data from external sources.
 * Following hexagonal architecture principles, this abstracts the external API client.
 *
 * @author Weather API Team
 */
public interface WeatherRepositoryPort {

    /**
     * Fetches current weather data from external API.
     *
     * @param latitude  the latitude coordinate
     * @param longitude the longitude coordinate
     * @param timezone  the timezone (or "auto")
     * @return Weather object with current conditions
     */
    Weather fetchWeatherForecast(Double latitude, Double longitude, String timezone);

    /**
     * Searches for cities in external geocoding API.
     *
     * @param cityName the city name to search for
     * @param count    maximum number of results
     * @param language language for results
     * @return List of geocoding results
     */
    List<GeocodingResult> searchCity(String cityName, Integer count, String language);
}
