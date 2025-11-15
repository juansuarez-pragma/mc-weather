package com.weather.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Domain model representing weather information.
 * This is the core business entity in the hexagonal architecture.
 *
 * @author Weather API Team
 */
@Value
@Builder
public class Weather {
    LocalDateTime time;
    Double temperature;
    Integer weatherCode;
    Double windSpeed;
    Integer humidity;
    Double latitude;
    Double longitude;
    String timezone;

    /**
     * Validates if the weather data is complete and valid.
     *
     * @return true if all required fields are present and valid
     */
    public boolean isValid() {
        return temperature != null
                && weatherCode != null
                && windSpeed != null
                && latitude != null
                && longitude != null
                && isValidCoordinates();
    }

    /**
     * Validates if coordinates are within valid ranges.
     *
     * @return true if coordinates are valid
     */
    private boolean isValidCoordinates() {
        return latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0;
    }
}
