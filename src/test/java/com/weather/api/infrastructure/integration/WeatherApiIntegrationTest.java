package com.weather.api.infrastructure.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.weather.api.application.dto.response.WeatherForecastResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Weather API using WireMock.
 * Tests the full stack from controller to external API client.
 *
 * @author Weather API Team
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "openmeteo.api.base-url=http://localhost:${wiremock.server.port}",
        "openmeteo.api.geocoding-url=http://localhost:${wiremock.server.port}"
})
@DisplayName("Weather API Integration Tests")
class WeatherApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Should fetch weather forecast successfully from external API")
    void shouldFetchWeatherForecastSuccessfully() {
        // Given
        String mockResponse = """
                {
                  "latitude": 40.7128,
                  "longitude": -74.006,
                  "timezone": "America/New_York",
                  "current": {
                    "time": "2025-11-15T10:30:00",
                    "temperature_2m": 15.5,
                    "weather_code": 2,
                    "wind_speed_10m": 12.3,
                    "relative_humidity_2m": 65
                  }
                }
                """;

        stubFor(get(urlPathEqualTo("/v1/forecast"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponse)));

        // When
        ResponseEntity<WeatherForecastResponse> response = restTemplate.getForEntity(
                "/api/v1/weather/forecast?latitude=40.7128&longitude=-74.0060",
                WeatherForecastResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getLatitude()).isEqualTo(40.7128);
        assertThat(response.getBody().getLongitude()).isEqualTo(-74.006);
        assertThat(response.getBody().getCurrent().getTemperature()).isEqualTo(15.5);
    }

    @Test
    @DisplayName("Should return 503 when external API is unavailable")
    void shouldReturn503WhenExternalApiUnavailable() {
        // Given
        stubFor(get(urlPathEqualTo("/v1/forecast"))
                .willReturn(aResponse()
                        .withStatus(503)));

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/weather/forecast?latitude=40.7128&longitude=-74.0060",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    @DisplayName("Should return 400 for invalid coordinates")
    void shouldReturn400ForInvalidCoordinates() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/weather/forecast?latitude=91.0&longitude=-74.0060",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should search cities successfully")
    void shouldSearchCitiesSuccessfully() {
        // Given
        String mockResponse = """
                {
                  "results": [
                    {
                      "name": "New York",
                      "latitude": 40.7128,
                      "longitude": -74.006,
                      "country": "United States",
                      "admin1": "New York"
                    }
                  ]
                }
                """;

        stubFor(get(urlPathEqualTo("/v1/search"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponse)));

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/weather/search?name=New York",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("New York");
    }

    @Test
    @DisplayName("Should cache weather forecast on second request")
    void shouldCacheWeatherForecast() {
        // Given
        String mockResponse = """
                {
                  "latitude": 40.7128,
                  "longitude": -74.006,
                  "timezone": "America/New_York",
                  "current": {
                    "time": "2025-11-15T10:30:00",
                    "temperature_2m": 15.5,
                    "weather_code": 2,
                    "wind_speed_10m": 12.3,
                    "relative_humidity_2m": 65
                  }
                }
                """;

        stubFor(get(urlPathEqualTo("/v1/forecast"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponse)));

        // When - First request
        ResponseEntity<WeatherForecastResponse> firstResponse = restTemplate.getForEntity(
                "/api/v1/weather/forecast?latitude=40.7128&longitude=-74.0060",
                WeatherForecastResponse.class
        );

        // When - Second request (should be cached)
        ResponseEntity<WeatherForecastResponse> secondResponse = restTemplate.getForEntity(
                "/api/v1/weather/forecast?latitude=40.7128&longitude=-74.0060",
                WeatherForecastResponse.class
        );

        // Then
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify only one call was made to external API (second was cached)
        WireMock.verify(1, WireMock.getRequestedFor(urlPathEqualTo("/v1/forecast")));
    }
}
