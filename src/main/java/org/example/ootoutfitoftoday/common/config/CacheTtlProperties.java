package org.example.ootoutfitoftoday.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 캐시 TTL 설정 Properties
 * yml의 cache.ttl 하위 설정을 자동으로 바인딩
 * 환경별로 다른 TTL 적용 가능(dev에서 override)
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "cache.ttl")
public final class CacheTtlProperties {

    // 사용자 정보 캐시 TTL
    // 기본값: 10분
    private Duration user = Duration.ofMinutes(10);

    // 사용자 존재 여부 캐시 TTL
    // 기본값: 1분(빈번한 조회, 짧은 캐싱)
    private Duration userExists = Duration.ofMinutes(1);

    // 판매 게시글 목록 캐시 TTL
    // 기본값: 10분
    private Duration salePostList = Duration.ofMinutes(10);

    // 대시보드 관리자용 캐시 TTL
    // 기본값: 3분
    private Duration dashboardAdmin = Duration.ofMinutes(3);

    // 대시보드 사용자 요약 정보 TTL
    // 기본값: 25시간(일 단위 통계)
    private Duration dashboardUserSummary = Duration.ofHours(25);

    // 대시보드 착용 통계 TTL
    // 기본값: 25시간(일 단위 통계)
    private Duration dashboardUserWearStatistics = Duration.ofHours(25);
}