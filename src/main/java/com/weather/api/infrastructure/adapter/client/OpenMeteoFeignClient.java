package com.weather.api.infrastructure.adapter.client;

import com.weather.api.infrastructure.adapter.client.dto.OpenMeteoGeocodingResponse;
import com.weather.api.infrastructure.adapter.client.dto.OpenMeteoWeatherResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for Open-Meteo API.
 * Defines the contract for communicating with external weather services.
 *
 * @author Weather API Team
 */
@FeignClient(
        name = "open-meteo-api",
        url = "${openmeteo.api.base-url}"
)
public interface OpenMeteoFeignClient {

    /**
     * Fetches weather forecast from Open-Meteo API.
     *
     * @param latitude  the latitude
     * @param longitude the longitude
     * @param current   comma-separated list of weather parameters
     * @param timezone  the timezone
     * @return weather response
     */
    @GetMapping("/v1/forecast")
    OpenMeteoWeatherResponse getWeatherForecast(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("current") String current,
            @RequestParam("timezone") String timezone
    );
}
