package org.example.ootoutfitoftoday.security.config;

/**
 * 보안 화이트리스트 중앙 관리 클래스
 * 목적:
 * - JWT 필터와 SecurityConfig에서 사용하는 경로를 한 곳에서 관리
 * - DRY(Don't Repeat Yourself) 원칙 준수
 * - 경로 변경 시 한 곳만 수정하면 되도록 보장
 * 설계 원칙:
 * - 각 화이트리스트는 용도별로 명확히 구분
 * - 인프라는 필터에서 제외, 비즈니스 API는 SecurityConfig에서만 관리
 */
public final class SecurityWhitelist {

    // 인스턴스화 방지(유틸리티 클래스이므로)
    private SecurityWhitelist() {
        throw new AssertionError("SecurityWhitelist는 인스턴스화할 수 없습니다.");
    }

    // =================================================================
    // 1. 인프라 경로(JWT 필터 제외 대상)
    // - JWT 검증 자체가 불필요한 경로
    // - 필터 실행 없이 바로 통과시켜 성능 최적화
    // =================================================================

    /**
     * Swagger 문서화 도구 경로
     * - API 문서를 제공하는 정적 리소스
     * - JWT 검증 불필요(공개 문서)
     */
    public static final String[] SWAGGER_PATHS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-resources/**",
            "/webjars/**"
    };
    /**
     * Actuator 모니터링 엔드포인트
     * - 헬스체크, 메트릭 수집용 엔드포인트
     * - JWT 검증 불필요(보안그룹/방화벽으로 접근 제어)
     */
    public static final String[] ACTUATOR_PATHS = {
            "/actuator/health",
            "/actuator/info",
            "/actuator/prometheus"
    };

    /**
     * OAuth2 프로토콜 엔드포인트
     * - Spring Security OAuth2가 자체적으로 인증 처리
     * - JWT 필터가 개입하면 안 됨
     */
    public static final String[] OAUTH2_PATHS = {
            "/oauth2/**",
            "/login/oauth2/**"
    };

    /**
     * WebSocket 엔드포인트
     * - WebSocket 연결은 별도 인증 메커니즘 사용
     * - JWT 필터에서 제외
     */
    public static final String[] WEBSOCKET_PATHS = {
            "/ws/**",
            "/stomp/**"
    };

    /**
     * 내부 API 엔드포인트
     * - 서버 간 통신용(API Gateway, 내부 서비스 등)
     * - 별도의 내부 인증 메커니즘 사용
     */
    public static final String[] INTERNAL_API_PATHS = {
            "/v1/internal/**"
    };

    // =================================================================
    // 2. 공개 API 경로(SecurityConfig에서 permitAll 처리)
    // - JWT 필터는 실행되지만 SecurityConfig에서 인증 불필요 설정
    // - 관대한 필터 전략에 따라 헤더 없으면 통과 → SecurityConfig가 최종 판단
    // =================================================================

    /**
     * 인증 관련 공개 API(POST)
     * - 회원가입, 로그인, 토큰 갱신 등
     * - 인증 전에 접근해야 하는 엔드포인트
     */
    public static final String[] AUTH_PUBLIC_POST_PATHS = {
            "/v1/auth/signup",
            "/v1/auth/login",
            "/v1/auth/refresh",
            "/v1/auth/oauth2/token/exchange",
            "/v1/auth/check/**"
    };

    /**
     * 조회용 공개 API(GET)
     * - 로그인 없이도 볼 수 있는 공개 정보
     * - 옷장, 판매 게시글, 카테고리, 기부센터 등
     */
    public static final String[] PUBLIC_GET_PATHS = {
            "/v1/closets/public/**",
            "/v1/sale-posts/public",
            "/v1/sale-posts/{salePostId}",
            "/v1/categories",
            "/v1/donation-centers/search"
    };

    // =================================================================
    // 3. 권한별 API 경로
    /// =================================================================

    /**
     * 관리자 전용 API
     * - ADMIN 권한 필수
     */
    public static final String[] ADMIN_PATHS = {
            "/admin/**"
    };

    // =================================================================
    // 4. 유틸리티 메서드
    // - 경로 배열을 하나로 합치는 헬퍼 메서드
    // =================================================================

    /**
     * 여러 경로 배열을 하나로 합치는 메서드
     * 사용 예:
     * String[] allInfra = combineArrays(SWAGGER_PATHS, ACTUATOR_PATHS);
     */
    public static String[] combineArrays(String[]... arrays) {
        int totalLength = 0;
        for (String[] array : arrays) {
            totalLength += array.length;
        }

        String[] result = new String[totalLength];
        int currentIndex = 0;

        for (String[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }

        return result;
    }

    /**
     * JWT 필터에서 제외할 모든 인프라 경로 반환
     */
    public static String[] getAllInfrastructurePaths() {
        return combineArrays(
                SWAGGER_PATHS,
                ACTUATOR_PATHS,
                OAUTH2_PATHS,
                WEBSOCKET_PATHS,
                INTERNAL_API_PATHS
        );
    }

    /**
     * SecurityConfig에서 permitAll 할 모든 경로 반환(인프라 포함)
     * 주의: 이 메서드는 모든 공개 경로를 반환하지만,
     *      HTTP 메서드별로 구분이 필요한 경우 개별 상수를 사용해야 함
     */
    public static String[] getAllPublicPaths() {
        return combineArrays(
                SWAGGER_PATHS,
                ACTUATOR_PATHS,
                OAUTH2_PATHS,
                WEBSOCKET_PATHS,
                INTERNAL_API_PATHS,
                AUTH_PUBLIC_POST_PATHS
                // 주의: PUBLIC_GET_PATHS는 GET 메서드로만 허용되므로
                // SecurityConfig에서 개별적으로 처리 필요
        );
    }
}