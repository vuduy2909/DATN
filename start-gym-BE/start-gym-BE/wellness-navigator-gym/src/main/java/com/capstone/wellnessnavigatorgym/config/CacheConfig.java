package com.capstone.wellnessnavigatorgym.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration cho tối ưu hóa hiệu suất
 * Giảm tải database khi có nhiều user
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache Manager với multiple cache regions
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Các cache regions khác nhau cho các loại dữ liệu khác nhau
        cacheManager.setCacheNames(Arrays.asList(
            "customerProgress",      // Cache 5 phút - tiến độ customer
            "dashboardStats",        // Cache 10 phút - thống kê dashboard
            "todayAttendance",       // Cache 1 ngày - điểm danh hôm nay
            "topPerformers",         // Cache 1 giờ - top performers
            "courseStats",           // Cache 30 phút - thống kê course
            "userProfile",           // Cache 15 phút - thông tin user
            "systemStats"            // Cache 5 phút - thống kê hệ thống
        ));
        
        return cacheManager;
    }

    /**
     * Custom Key Generator cho cache
     */
    @Bean
    public KeyGenerator customKeyGenerator() {
        return new SimpleKeyGenerator();
    }
}

/**
 * Nếu muốn sử dụng Redis cache (production), 
 * thêm dependency và config sau:
 * 
 * @Bean
 * public LettuceConnectionFactory redisConnectionFactory() {
 *     return new LettuceConnectionFactory(
 *         new RedisStandaloneConfiguration("localhost", 6379));
 * }
 * 
 * @Bean
 * public RedisCacheManager cacheManager() {
 *     RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
 *         .entryTtl(Duration.ofMinutes(10))
 *         .serializeKeysWith(RedisSerializationContext.SerializationPair
 *             .fromSerializer(new StringRedisSerializer()))
 *         .serializeValuesWith(RedisSerializationContext.SerializationPair
 *             .fromSerializer(new GenericJackson2JsonRedisSerializer()));
 * 
 *     return RedisCacheManager.builder(redisConnectionFactory())
 *         .cacheDefaults(cacheConfig)
 *         .build();
 * }
 */ 