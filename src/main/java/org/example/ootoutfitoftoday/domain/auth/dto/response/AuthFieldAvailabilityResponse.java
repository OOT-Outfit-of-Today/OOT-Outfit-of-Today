package org.example.ootoutfitoftoday.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 실시간 중복 체크 응답 DTO
 * - 회원가입 시 개별 필드의 중복 여부를 즉시 확인하는 API 응답
 * - 사용자 경험 개선: 입력 완료 시 즉각적인 피드백 제공
 */
@Getter
@AllArgsConstructor
public class AuthFieldAvailabilityResponse {

    /**
     * 필드 사용 가능 여부
     * - true: 사용 가능(중복 없음)
     * - false: 사용 불가(중복 있음)
     */
    private final boolean available;
}