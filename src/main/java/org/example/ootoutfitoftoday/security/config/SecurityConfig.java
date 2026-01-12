package org.example.ootoutfitoftoday.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;
import org.example.ootoutfitoftoday.security.filter.JwtAuthenticationFilter;
import org.example.ootoutfitoftoday.security.oauth2.CustomOAuth2UserService;
import org.example.ootoutfitoftoday.security.oauth2.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final ObjectMapper objectMapper;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${frontend.url}")
    private String frontendUrl;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Lazy OAuth2SuccessHandler oAuth2SuccessHandler,
            CustomOAuth2UserService customOAuth2UserService,
            ObjectMapper objectMapper
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder(9);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cookie",
                "Set-Cookie"
        ));
        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 보안 정책의 단일 진실 공급원(Single Source of Truth)
     * 설계 원칙:
     * - 모든 인가(Authorization) 정책은 이곳에서만 관리
     * - JwtAuthenticationFilter는 JWT 검증만 담당(정책 결정 X)
     * - SecurityWhitelist 클래스에서 경로 상수를 중앙 관리
     *   - 경로 변경 시 SecurityWhitelist만 수정하면 됨
     *   - DRY 원칙 준수로 유지보수성 향상
     * 구조:
     * 1. 인프라 엔드포인트(Swagger, Actuator 등)
     * 2. 공개 API(회원가입, 로그인 등)
     * 3. OAuth2 엔드포인트
     * 4. WebSocket 엔드포인트
     * 5. 관리자 전용 API
     * 6. 내부 API(서버 간 통신)
     * 7. 나머지 모든 API(인증 필수)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .addFilterBefore(jwtAuthenticationFilter, SecurityContextHolderAwareRequestFilter.class)

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .rememberMe(AbstractHttpConfigurer::disable)

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/oauth2") || request.getRequestURI().startsWith("/login/oauth2")) {
                                response.sendRedirect(frontendUrl + "/login?error=auth_failed");

                                return;
                            }
                            writeErrorResponse(response, request, HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeErrorResponse(response, request, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."))
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 로그인 실패", exception);

                            HttpSession session = request.getSession(false);
                            if (session != null) {
                                log.error("세션 정보 - ID: {}, CreationTime: {}, LastAccessedTime: {}", session.getId(), new Date(session.getCreationTime()), new Date(session.getLastAccessedTime()));
                            } else {
                                log.error("세션을 찾을 수 없습니다");
                            }

                            String encodedMessage = URLEncoder.encode("OAuth2 인증 실패: " + exception.getMessage(), StandardCharsets.UTF_8);

                            response.sendRedirect(frontendUrl + "/login?error=" + encodedMessage);
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        // 인프라: 문서화 도구(Swagger)
                        // API 문서는 누구나 접근 가능
                        .requestMatchers(SecurityWhitelist.SWAGGER_PATHS).permitAll()

                        // 인프라: 모니터링 엔드포인트(Actuator)
                        // 헬스체크, 메트릭 수집은 인증 불필요(보안그룹/방화벽으로 제한)
                        .requestMatchers(SecurityWhitelist.ACTUATOR_PATHS).permitAll()

                        // 공개 API: 인증(회원가입, 로그인, 토큰 갱신, 실시간 중복 체크)
                        // POST 메서드로만 접근 가능
                        .requestMatchers(HttpMethod.POST, SecurityWhitelist.AUTH_PUBLIC_POST_PATHS).permitAll()

                        // 공개 API: 조회(옷장, 판매 게시글, 카테고리, 기부센터)
                        // 로그인 없이도 볼 수 있는 공개 정보
                        .requestMatchers(HttpMethod.GET, SecurityWhitelist.PUBLIC_GET_PATHS).permitAll()

                        // OAuth2: 소셜 로그인 프로토콜 엔드포인트
                        // Spring Security OAuth2가 자체적으로 처리
                        .requestMatchers(SecurityWhitelist.OAUTH2_PATHS).permitAll()

                        // WebSocket: 실시간 통신 엔드포인트
                        // WebSocket은 연결 후 별도 인증 메커니즘 사용
                        .requestMatchers(SecurityWhitelist.WEBSOCKET_PATHS).permitAll()

                        // 관리자 전용 API
                        // 관리자 권한 필수
                        .requestMatchers(SecurityWhitelist.ADMIN_PATHS).hasAuthority(UserRole.Authority.ADMIN)

                        // 내부 API: 서버 간 통신
                        // API Gateway 등에서 별도 인증 메커니즘 사용
                        .requestMatchers(SecurityWhitelist.INTERNAL_API_PATHS).permitAll()

                        // 나머지 모든 API: 인증 필수
                        // 명시적으로 허용되지 않은 모든 엔드포인트는 인증 필요
                        .anyRequest().authenticated()
                )
                .build();
    }

    private void writeErrorResponse(
            HttpServletResponse response,
            HttpServletRequest request,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("httpStatus", status.name());
        errorResponse.put("statusValue", status.value());
        errorResponse.put("success", false);
        errorResponse.put("code", status == HttpStatus.UNAUTHORIZED ? "AUTHENTICATION_ERROR" : "ACCESS_DENIED");
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}