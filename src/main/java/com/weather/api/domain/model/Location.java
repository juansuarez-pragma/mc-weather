package com.weather.api.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Domain model representing geographical coordinates.
 *
 * @author Weather API Team
 */
@Value
@Builder
public class Location {
    Double latitude;
    Double longitude;

    /**
     * Validates if coordinates are within valid geographical ranges.
     *
     * @return true if coordinates are valid
     */
    public boolean isValid() {
        return latitude != null && longitude != null
                && latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0;
    }

    /**
     * Calculates distance to another location using Haversine formula.
     *
     * @param other the other location
     * @return distance in kilometers
     */
    public double distanceTo(Location other) {
        final int EARTH_RADIUS = 6371; // kilometers

        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    @Override
    public String toString() {
        return String.format("(%.4f, %.4f)", latitude, longitude);
    }
}
