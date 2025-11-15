package com.weather.api.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for weather forecast.
 *
 * @author Weather API Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Weather forecast response")
public class WeatherForecastResponse {

    @Schema(description = "Latitude coordinate", example = "40.7128")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "-74.0060")
    private Double longitude;

    @Schema(description = "Timezone", example = "America/New_York")
    private String timezone;

    @Schema(description = "Current weather conditions")
    private CurrentWeatherDTO current;
}
