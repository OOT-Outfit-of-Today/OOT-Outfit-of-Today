package org.example.ootoutfitoftoday.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;
import org.example.ootoutfitoftoday.security.jwt.JwtAuthenticationToken;
import org.example.ootoutfitoftoday.security.jwt.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /**
     * JWT 필터 실행 제외 경로(인프라/도구만 포함)
     * 설계 원칙: 필터는 JWT 검증만 담당, 보안 정책은 SecurityConfig에 위임
     * - 이 메서드는 "필터 실행이 아예 불필요한 경로"만 정의
     * - 비즈니스 API는 제외(SecurityConfig에서 인가 처리)
     * - 성능 최적화 목적: Swagger, Actuator 등은 JWT 검증 자체가 무의미
     */

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // 인프라: 문서화 도구 (Swagger)
        // JWT 검증이 아예 불필요한 정적 리소스
        if (path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars")) {

            return true;
        }

        // 인프라: 모니터링 엔드포인트 (Actuator)
        // 헬스체크, 메트릭 수집은 JWT 없이 동작해야 함
        if (path.startsWith("/actuator/health") ||
                path.startsWith("/actuator/info") ||
                path.startsWith("/actuator/prometheus")) {

            return true;
        }

        // 인프라: OAuth2 프로토콜 엔드포인트
        // Spring Security OAuth2가 자체적으로 처리
        if (path.startsWith("/oauth2/") ||
                path.startsWith("/login/oauth2/")) {

            return true;
        }

        // 인프라: WebSocket 엔드포인트
        // WebSocket은 별도 인증 메커니즘 사용
        if (path.startsWith("/ws")
                || path.startsWith("/stomp")) {

            return true;
        }

        // 내부 API: 서버 간 통신
        // 별도의 내부 인증 메커니즘 사용(API Gateway 등)
        if (path.startsWith("/v1/internal/")) {
            log.debug("[JWT FILTER] Skipped for Internal API → {}", path);

            return true;
        }

        // 나머지 모든 비즈니스 API는 필터 실행
        // 헤더 없으면 통과, 있으면 검증(doFilterInternal에서 처리)

        return false;
    }

    /**
     * JWT 인증 처리(관대한 필터 전략)
     * 설계 원칙:
     * 1. Authorization 헤더 없음 → 통과(SecurityConfig에 위임)
     * 2. Authorization 헤더 있음 → 반드시 검증(잘못된 토큰은 차단)
     * 3. 최종 인가 결정은 SecurityConfig의 authorizeHttpRequests()가 담당
     * 이점:
     * - 보안 정책의 단일 진실 공급원(Single Source of Truth) = SecurityConfig
     * - 새로운 공개 API 추가 시 SecurityConfig만 수정하면 됨
     * - 다중 인증 방식 지원 가능(JWT, OAuth2, API Key 등)
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest httpRequest,
            @NonNull HttpServletResponse httpResponse,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {
        log.info("JwtAuthenticationFilter 진입: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());

        // try-finally 블록으로 감싸서 SecurityContext 정리 보장
        try {
            String authorizationHeader = httpRequest.getHeader("Authorization");

            // Authorization 헤더 없음 → 통과(SecurityConfig에서 최종 판단)
            // 공개 API인지 인증 필요한지는 SecurityConfig의 authorizeHttpRequests()가 결정
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                log.debug("Authorization 헤더 없음 → SecurityConfig에 위임: {}", httpRequest.getRequestURI());
                chain.doFilter(httpRequest, httpResponse);

                return;
            }

            // Authorization 헤더 있음 → JWT 검증 필수
            // 잘못된 토큰으로 접근 시도는 여기서 차단
            String jwt = jwtUtil.substringToken(authorizationHeader);

            if (!processAuthentication(jwt, httpRequest, httpResponse)) {
                // 검증 실패 시 에러 응답 후 종료
                return;
            }

            // JWT 검증 성공 → 다음 필터로
            chain.doFilter(httpRequest, httpResponse);

        } finally {
            // 이중 안전장치: 요청 처리 완료 후 반드시 SecurityContext 정리
            // Spring Security가 자동으로 clear하지만, ThreadLocal 오염 방지를 위해 명시적으로 정리
            // 예외 발생 시에도 반드시 실행되어 Thread 재사용 시 이전 인증 정보가 남지 않도록 보장
            SecurityContextHolder.clearContext();
            log.debug("SecurityContext 명시적 정리 완료");
        }
    }

    private boolean processAuthentication(
            String jwt,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        try {
            if (!jwtUtil.isAccessToken(jwt)) {
                log.warn("리프레시 토큰이 Authorization 헤더로 전송됨: URI={}", request.getRequestURI());
                sendErrorResponse(response, HttpStatus.BAD_REQUEST, "액세스 토큰이 필요합니다.");

                return false;
            }

            Claims claims = jwtUtil.extractClaims(jwt);

            // 로그 추가(디버깅용)
            log.debug("JWT 토큰 파싱 완료 - userId: {}, URI: {}", claims.getSubject(), request.getRequestURI());

            // 항상 새로운 Authentication 설정
            setAuthentication(claims);

            return true;

        } catch (SignatureException e) {
            log.warn("JWT 서명 불일치: URI={}", request.getRequestURI(), e);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 서명입니다.");

            return false;

        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 형식: URI={}", request.getRequestURI(), e);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "잘못된 JWT 토큰입니다.");

            return false;

        } catch (ExpiredJwtException e) {
            log.warn("JWT 만료: userId={}, URI={}", e.getClaims().getSubject(), request.getRequestURI());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다.");

            return false;

        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT: URI={}", request.getRequestURI(), e);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "지원되지 않는 JWT 토큰입니다.");

            return false;

        } catch (Exception e) {
            log.error("예상치 못한 JWT 검증 오류: URI={}", request.getRequestURI(), e);
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

            return false;
        }
    }

    private void setAuthentication(Claims claims) {
        Long userId = Long.valueOf(claims.getSubject());
        UserRole userRole = UserRole.of(claims.get("userRole", String.class));

        AuthUser authUser = new AuthUser(userId, userRole);
        Authentication authenticationToken = new JwtAuthenticationToken(authUser);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private void sendErrorResponse(
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.name());
        errorResponse.put("code", status.value());
        errorResponse.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
