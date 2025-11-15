package com.weather.api.domain.exception;

/**
 * Base exception for all weather API domain exceptions.
 *
 * @author Weather API Team
 */
public class WeatherApiException extends RuntimeException {

    public WeatherApiException(String message) {
        super(message);
    }

    public WeatherApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
