package com.weather.api.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Domain model representing a geocoding search result.
 *
 * @author Weather API Team
 */
@Value
@Builder
public class GeocodingResult {
    String id;
    String name;
    Double latitude;
    Double longitude;
    String country;
    String admin1; // State or province

    /**
     * Generates a display name for the location.
     * Format: "Name, State, Country" or "Name, Country" if state is null.
     *
     * @return formatted display name
     */
    public String getDisplayName() {
        StringBuilder displayName = new StringBuilder(name);

        if (admin1 != null && !admin1.isEmpty()) {
            displayName.append(", ").append(admin1);
        }

        if (country != null && !country.isEmpty()) {
            displayName.append(", ").append(country);
        }

        return displayName.toString();
    }

    /**
     * Gets the location coordinates.
     *
     * @return Location object with coordinates
     */
    public Location getLocation() {
        return Location.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    /**
     * Validates if the geocoding result has valid data.
     *
     * @return true if all required fields are present and valid
     */
    public boolean isValid() {
        return name != null && !name.isEmpty()
                && latitude != null && longitude != null
                && latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0;
    }
}
