package org.example.ootoutfitoftoday.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ootcommon.dashboard.constant.DashboardAdminCacheNames;
import com.ootcommon.dashboard.constant.DashboardUserCacheNames;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.constant.CacheNames;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Redis 설정
 * Spring Boot 자동 설정을 최대한 활용하고 필요한 부분만 커스터마이징
 * 자동 설정 활용:
 * - RedisConnectionFactory: yml의 spring.data.redis 설정 자동 적용
 * - RedisTemplate: Spring Boot 기본 설정 사용
 * - Lettuce 커넥션 풀: yml의 lettuce.pool 설정 자동 적용
 * 커스터마이징:
 * - CacheManager: Redis 전용 ObjectMapper + 캐시별 TTL 설정
 */
@EnableCaching
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final CacheTtlProperties cacheTtlProperties;

    /**
     * 전역 ObjectMapper(JSON 직렬화 공통 설정)
     * - JavaTimeModule: LocalDateTime 등 Java 8 시간 타입 지원
     * - WRITE_DATES_AS_TIMESTAMPS 비활성화: ISO-8601 문자열 형식으로 날짜 직렬화
     */
    @Bean
    @Primary
    public ObjectMapper globalObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Redis용 ObjectMapper(타입 정보 포함)
     * - LaissezFaireSubTypeValidator: 역직렬화 시 모든 타입 허용
     * - DefaultTyping.NON_FINAL: final이 아닌 타입에 대해 타입 정보 포함
     * - JsonTypeInfo.As.PROPERTY: 타입 정보를 "@class" 프로퍼티로 포함
     * 이렇게 하면 Redis에서 객체를 꺼낼 때 원래 타입으로 복원 가능
     */
    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }

    /**
     * Redis 캐시 설정 커스터마이징
     * Spring Boot의 자동 설정된 RedisCacheManager를 커스터마이징
     * yml 파일의 기본 설정(cache-null-values, key-prefix, time-to-live)
     * -> 자동으로 적용, 캐시별 TTL만 추가로 설정
     *
     * @return RedisCacheManagerBuilderCustomizer
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(CacheProperties cacheProperties) {
        // Redis 전용 ObjectMapper로 커스터마이징 Serializer 생성
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        return (builder) -> {

            // yml의 기본 TTL 가져오기(application.yml의 time-to-live)
            Duration defaultTtl = cacheProperties.getRedis().getTimeToLive();    // yml 값 사용

            RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(defaultTtl)    // yml 값 사용
                    .serializeValuesWith(
                            RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                    );

            // 기본 설정 적용
            builder.cacheDefaults(defaultConfig);

            // 캐시별 커스텀 TTL 설정(개별 설정 안 한 캐시는 기본 TTL 사용)
            // 모두 상수 + Properties 조합
            builder
                    .withCacheConfiguration(CacheNames.USER,
                            defaultConfig.entryTtl(cacheTtlProperties.getUser()))
                    .withCacheConfiguration(CacheNames.USER_EXISTS,
                            defaultConfig.entryTtl(cacheTtlProperties.getUserExists()))
                    .withCacheConfiguration(CacheNames.SALE_POST_LIST,
                            defaultConfig.entryTtl(cacheTtlProperties.getSalePostList()))
                    .withCacheConfiguration(DashboardAdminCacheNames.USER,
                            defaultConfig.entryTtl(cacheTtlProperties.getDashboardAdmin()))
                    .withCacheConfiguration(DashboardAdminCacheNames.CLOTHES,
                            defaultConfig.entryTtl(cacheTtlProperties.getDashboardAdmin()))
                    .withCacheConfiguration(DashboardAdminCacheNames.SALE_POST,
                            defaultConfig.entryTtl(cacheTtlProperties.getDashboardAdmin()))
                    .withCacheConfiguration(DashboardAdminCacheNames.CATEGORY,
                            defaultConfig.entryTtl(cacheTtlProperties.getDashboardAdmin()))
                    .withCacheConfiguration(DashboardUserCacheNames.SUMMARY,
                            defaultConfig.entryTtl(cacheTtlProperties.getDashboardUserSummary()))
                    .withCacheConfiguration(DashboardUserCacheNames.WEAR_STATISTICS,
                            defaultConfig.entryTtl(cacheTtlProperties.getDashboardUserWearStatistics()));
        };
    }
}