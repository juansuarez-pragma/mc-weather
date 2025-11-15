package com.weather.api.application.mapper;

import com.weather.api.application.dto.response.CitySearchResponse;
import com.weather.api.application.dto.response.GeocodingResultDTO;
import com.weather.api.domain.model.GeocodingResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for transforming between GeocodingResult domain models and DTOs.
 *
 * @author Weather API Team
 */
@Component
public class GeocodingMapper {

    /**
     * Converts list of GeocodingResult domain models to CitySearchResponse DTO.
     *
     * @param results the list of domain models
     * @return the response DTO
     */
    public CitySearchResponse toResponse(List<GeocodingResult> results) {
        if (results == null) {
            return CitySearchResponse.builder()
                    .results(List.of())
                    .build();
        }

        List<GeocodingResultDTO> dtos = results.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return CitySearchResponse.builder()
                .results(dtos)
                .build();
    }

    /**
     * Converts GeocodingResult domain model to GeocodingResultDTO.
     *
     * @param result the domain model
     * @return the DTO
     */
    private GeocodingResultDTO toDTO(GeocodingResult result) {
        if (result == null) {
            return null;
        }

        return GeocodingResultDTO.builder()
                .id(result.getId())
                .name(result.getName())
                .latitude(result.getLatitude())
                .longitude(result.getLongitude())
                .country(result.getCountry())
                .admin1(result.getAdmin1())
                .displayName(result.getDisplayName())
                .build();
    }
}
