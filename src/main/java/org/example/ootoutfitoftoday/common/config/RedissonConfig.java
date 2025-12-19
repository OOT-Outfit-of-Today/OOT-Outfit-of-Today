package org.example.ootoutfitoftoday.common.config;

import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redisson 설정(분산 락용)
 * 설정 구조:
 * - RedisProperties: Spring Boot 제공(spring.data.redis -> Redis 연결 정보)
 *   - yml이 .env 환경변수를 참조(spring.data.redis.host: ${REDIS_HOST})
 *   - 이 클래스는 yml 설정을 읽어서 Redisson 구성에 사용
 *   - RedisConfig와 동일한 Redis 서버에 연결
 * - RedissonProperties: 커스텀 구현(redisson -> Redisson 전용 설정)
 * 일관된 Properties 패턴:
 * - 모든 설정을 Properties 클래스로 관리
 * - @Value 사용하지 않음
 * - YAML 중앙 집중식 관리
 * - 환경별 설정 override 가능
 */
@Configuration
@RequiredArgsConstructor
public class RedissonConfig {

    // Spring Boot가 제공하는 Redis 설정
    // spring.data.redis 하위 설정 자동 바인딩
    private final RedisProperties redisProperties;

    // 커스텀 Redisson 설정
    // redisson 하위 설정 자동 바인딩
    private final RedissonProperties redissonProperties;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        var singleServerConfig = config.useSingleServer()
                // Redis 연결 정보
                .setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort())
                // 커넥션 풀 설정
                .setConnectionPoolSize(redisProperties.getLettuce().getPool().getMaxActive())
                .setConnectionMinimumIdleSize(redisProperties.getLettuce().getPool().getMinIdle())
                // Redisson 전용 설정
                .setIdleConnectionTimeout((int) redissonProperties.getIdleConnectionTimeout().toMillis())
                // 타임아웃 설정
                .setConnectTimeout((int) redisProperties.getTimeout().toMillis())
                .setTimeout((int) redisProperties.getTimeout().toMillis())
                // 재시도 설정
                .setRetryAttempts(redissonProperties.getRetryAttempts())
                // deprecated 되었으나, 신규의 setRetryInterval(Duration) 사용 불가
                .setRetryInterval((int) redissonProperties.getRetryInterval().toMillis());

        // 비밀번호 설정
        if (StringUtils.hasText(redisProperties.getPassword())) {
            singleServerConfig.setPassword(redisProperties.getPassword());
        }

        return Redisson.create(config);
    }
}