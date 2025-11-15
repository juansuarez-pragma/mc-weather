package com.weather.api.domain.exception;

/**
 * Exception thrown when external API (Open-Meteo) fails or returns an error.
 *
 * @author Weather API Team
 */
public class ExternalApiException extends WeatherApiException {

    private final int statusCode;

    public ExternalApiException(String message) {
        super(message);
        this.statusCode = 500;
    }

    public ExternalApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 503;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
