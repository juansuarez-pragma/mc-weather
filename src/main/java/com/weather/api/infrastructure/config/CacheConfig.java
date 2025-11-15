package com.weather.api.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Caffeine cache.
 * Configures caching strategy for weather data and city searches.
 *
 * @author Weather API Team
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configures Caffeine cache manager with custom settings.
     *
     * @return configured cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        log.info("Configuring Caffeine cache manager");

        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "weatherForecast",
                "citySearch"
        );

        cacheManager.setCaffeine(caffeineCacheBuilder());

        return cacheManager;
    }

    /**
     * Builds Caffeine cache with specified configuration.
     *
     * @return Caffeine builder
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.debug("Cache eviction: key={}, cause={}", key, cause)
                );
    }
}
