package org.example.ootoutfitoftoday.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 회원가입 중복 체크용 Projection DTO
 * Repository 내부에서만 사용되며, User 엔티티 전체 조회 대신
 * 필요한 4개 필드만 조회하여 성능 최적화
 * - User 엔티티 전체 조회로 인한 불필요한 연관관계 로딩 방지
 * - TODO 1. 데이터 전송량: 감소율 확인(18개 필드 -> 4개 필드)
 *        2. 메모리 사용량: 감소율 확인
 *        3. record로 리팩토링 고려
 */
@Getter
@AllArgsConstructor
public class UserDuplicateFieldsProjection {

    private final String loginId;
    private final String email;
    private final String nickname;
    private final String phoneNumber;
}