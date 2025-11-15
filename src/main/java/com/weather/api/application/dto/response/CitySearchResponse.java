package com.weather.api.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for city search.
 *
 * @author Weather API Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "City search response")
public class CitySearchResponse {

    @Schema(description = "List of matching cities")
    private List<GeocodingResultDTO> results;
}
