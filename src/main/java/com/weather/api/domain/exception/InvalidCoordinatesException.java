package com.weather.api.domain.exception;

/**
 * Exception thrown when geographical coordinates are invalid.
 * Latitude must be between -90 and 90.
 * Longitude must be between -180 and 180.
 *
 * @author Weather API Team
 */
public class InvalidCoordinatesException extends WeatherApiException {

    public InvalidCoordinatesException(String message) {
        super(message);
    }

    public InvalidCoordinatesException(Double latitude, Double longitude) {
        super(String.format(
                "Invalid coordinates: latitude=%.4f (must be -90 to 90), longitude=%.4f (must be -180 to 180)",
                latitude, longitude
        ));
    }
}
