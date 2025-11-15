package com.weather.api.infrastructure.adapter.client;

import com.weather.api.infrastructure.adapter.client.dto.OpenMeteoGeocodingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for Open-Meteo Geocoding API.
 *
 * @author Weather API Team
 */
@FeignClient(
        name = "open-meteo-geocoding",
        url = "${openmeteo.api.geocoding-url}"
)
public interface OpenMeteoGeocodingFeignClient {

    /**
     * Searches for cities by name.
     *
     * @param name     the city name
     * @param count    maximum number of results
     * @param language language for results
     * @param format   response format
     * @return geocoding response
     */
    @GetMapping("/v1/search")
    OpenMeteoGeocodingResponse searchCity(
            @RequestParam("name") String name,
            @RequestParam("count") Integer count,
            @RequestParam("language") String language,
            @RequestParam("format") String format
    );
}
