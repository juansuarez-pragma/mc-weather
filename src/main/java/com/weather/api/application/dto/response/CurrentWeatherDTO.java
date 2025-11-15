package com.weather.api.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for current weather conditions.
 *
 * @author Weather API Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Current weather conditions")
public class CurrentWeatherDTO {

    @Schema(description = "Timestamp of weather data", example = "2025-11-15T10:30:00")
    private LocalDateTime time;

    @Schema(description = "Temperature in Celsius", example = "15.5")
    private Double temperature;

    @JsonProperty("weatherCode")
    @Schema(description = "WMO weather code", example = "2")
    private Integer weatherCode;

    @JsonProperty("windSpeed")
    @Schema(description = "Wind speed in km/h", example = "12.3")
    private Double windSpeed;

    @Schema(description = "Relative humidity percentage", example = "65")
    private Integer humidity;
}
