package com.weather.api.domain.exception;

/**
 * Exception thrown when a city search returns no results.
 *
 * @author Weather API Team
 */
public class CityNotFoundException extends WeatherApiException {

    public CityNotFoundException(String cityName) {
        super(String.format("No results found for city: %s", cityName));
    }

    public CityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
