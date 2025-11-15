package com.weather.api.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for city search endpoint.
 *
 * @author Weather API Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request parameters for city search")
public class CitySearchRequest {

    @NotBlank(message = "City name is required")
    @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters")
    @Schema(description = "City name to search", example = "New York", required = true)
    private String name;

    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 20, message = "Count must not exceed 20")
    @Schema(description = "Maximum number of results", example = "10", defaultValue = "10")
    @Builder.Default
    private Integer count = 10;

    @Schema(description = "Language for results", example = "en", defaultValue = "en")
    @Builder.Default
    private String language = "en";
}
