package com.weather.api.domain.port.input;

import com.weather.api.domain.model.GeocodingResult;
import com.weather.api.domain.model.Weather;

import java.util.List;

/**
 * Input port (use case interface) for weather operations.
 * This interface defines the business operations that can be performed.
 * Following hexagonal architecture principles, this is the entry point to the domain.
 *
 * @author Weather API Team
 */
public interface WeatherUseCase {

    /**
     * Retrieves current weather forecast for given coordinates.
     *
     * @param latitude  the latitude coordinate (-90 to 90)
     * @param longitude the longitude coordinate (-180 to 180)
     * @param timezone  optional timezone (default: "auto")
     * @return Weather object with current conditions
     * @throws com.weather.api.domain.exception.InvalidCoordinatesException if coordinates are invalid
     * @throws com.weather.api.domain.exception.ExternalApiException if external API fails
     */
    Weather getWeatherForecast(Double latitude, Double longitude, String timezone);

    /**
     * Searches for cities by name.
     *
     * @param cityName the city name to search for (minimum 2 characters)
     * @param count    maximum number of results (default: 10, max: 20)
     * @param language language for results (default: "en")
     * @return List of geocoding results
     * @throws com.weather.api.domain.exception.CityNotFoundException if no results found
     * @throws IllegalArgumentException if cityName is too short
     */
    List<GeocodingResult> searchCity(String cityName, Integer count, String language);
}
