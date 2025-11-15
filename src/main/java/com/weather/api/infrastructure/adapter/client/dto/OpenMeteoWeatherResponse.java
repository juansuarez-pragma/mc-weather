package com.weather.api.infrastructure.adapter.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Open-Meteo API weather response.
 *
 * @author Weather API Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenMeteoWeatherResponse {

    private Double latitude;

    private Double longitude;

    private String timezone;

    @JsonProperty("current")
    private CurrentData current;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentData {

        private String time;

        @JsonProperty("temperature_2m")
        private Double temperature;

        @JsonProperty("weather_code")
        private Integer weatherCode;

        @JsonProperty("wind_speed_10m")
        private Double windSpeed;

        @JsonProperty("relative_humidity_2m")
        private Integer humidity;
    }
}
