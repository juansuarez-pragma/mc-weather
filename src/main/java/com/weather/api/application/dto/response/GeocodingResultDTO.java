package com.weather.api.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for geocoding search result.
 *
 * @author Weather API Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Geocoding search result")
public class GeocodingResultDTO {

    @Schema(description = "Unique identifier for this result", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "City name", example = "New York")
    private String name;

    @Schema(description = "Latitude coordinate", example = "40.7128")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "-74.0060")
    private Double longitude;

    @Schema(description = "Country name", example = "United States")
    private String country;

    @Schema(description = "State or province name", example = "New York")
    private String admin1;

    @Schema(description = "Full display name", example = "New York, New York, United States")
    private String displayName;
}
