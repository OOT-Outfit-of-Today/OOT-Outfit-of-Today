package org.example.ootoutfitoftoday.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Redisson 설정 Properties
 * yaml의 redisson 하위 설정을 자동으로 바인딩
 * yaml에 값이 없으면 기본값 사용
 * 환경별로 다른 설정 적용 가능(dev에서 override)
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "redisson")
public final class RedissonProperties {

    // 유휴 연결 타임아웃(초)
    // 연결이 이 시간 동안 사용되지 않으면 종료
    // 기본값: 10초
    private Duration idleConnectionTimeout = Duration.ofSeconds(10);

    // 재시도 횟수
    // Redis 연결 실패 시 재시도 횟수
    // 기본값: 3회
    private int retryAttempts = 3;

    // 재시도 간격(밀리초)
    // 각 재시도 사이의 대기 시간
    // 기본값: 1.5초
    private Duration retryInterval = Duration.ofMillis(1500);
}
