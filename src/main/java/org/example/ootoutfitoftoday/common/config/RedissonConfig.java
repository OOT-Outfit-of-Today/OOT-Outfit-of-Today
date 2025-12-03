package org.example.ootoutfitoftoday.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Redisson 설정(분산 락용)
 * Redis 연결 정보를 .env에서 직접 읽어옴
 * RedisConfig와 동일한 환경변수를 사용하여 같은 Redis 서버에 연결
 * Redisson은 Redis 기반 분산 락 구현에 사용
 */
@Configuration
public class RedissonConfig {

    // 민감정보: .env 파일에서 환경변수로 주입
    @Value("${REDIS_HOST}")
    private String host;

    @Value("${REDIS_PORT}")
    private int port;

    @Value("${REDIS_PASSWORD}")
    private String password;

    // 설정값: yml 파일에서 프로퍼티로 주입(환경별 다른 값)
    // 환경별 커넥션 풀 설정값 주입(local=8, dev=10) -> 기본값 dev 기준
    @Value("${spring.data.redis.pool.max-active:}")
    private int connectionPoolSize;

    @Value("${spring.data.redis.pool.min-idle}")
    private int connectionMinimumIdleSize;

    // 공통 설정: application.yml에서 주입
    // Redis 타임아웃 설정값 주입
    @Value("${spring.data.redis.timeout}")
    private Duration timeout;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        var singleServerConfig = config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                // yml에서 주입받은 환경별 커넥션 풀 설정 적용
                .setConnectionPoolSize(connectionPoolSize)
                .setConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setIdleConnectionTimeout(10000)
                // yml에서 주입받은 타임아웃 설정 적용
                .setConnectTimeout((int) timeout.toMillis())
                .setTimeout((int) timeout.toMillis())
                .setRetryAttempts(3)
                // deprecated 되었으나, 신규의 setRetryInterval(Duration) 사용 불가
                .setRetryInterval(1500);

        if (StringUtils.hasText(password)) {
            singleServerConfig.setPassword(password);
        }

        return Redisson.create(config);
    }
}