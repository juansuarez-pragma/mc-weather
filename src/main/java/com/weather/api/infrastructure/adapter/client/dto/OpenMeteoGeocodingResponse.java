package com.weather.api.infrastructure.adapter.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Open-Meteo Geocoding API response.
 *
 * @author Weather API Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenMeteoGeocodingResponse {

    private List<GeocodingResult> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeocodingResult {

        private String name;

        private Double latitude;

        private Double longitude;

        private String country;

        private String admin1;
    }
}
