package org.example.ootoutfitoftoday.domain.user.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 회원가입 시 중복 검증 결과를 담는 DTO
 * - 단일 쿼리로 4개 필드(loginId, email, nickname, phoneNumber)의
 *   중복 여부를 동시에 확인한 결과를 표현
 * - 서버 내부 레이어 간 데이터 전달하는 Internal DTO(내부 전송)
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)    // 외부 생성 방지, 팩토리 메서드만 허용
public class UserDuplicateCheckResult {

    // 중복된 필드명 목록
    private final List<String> duplicateFields;

    // 중복 없이 사용 가능한 결과 생성
    public static UserDuplicateCheckResult noDuplicates() {
        return new UserDuplicateCheckResult(new ArrayList<>());
    }

    // 중복이 있는 결과 생성
    public static UserDuplicateCheckResult withDuplicates(List<String> duplicateFields) {
        return new UserDuplicateCheckResult(new ArrayList<>(duplicateFields)
        );
    }

    // 하나라도 중복이 있는지 확인
    public boolean hasDuplicates() {
        return !duplicateFields.isEmpty();
    }

    // 특정 필드가 중복인지 확인
    public boolean isDuplicate(String fieldName) {
        return duplicateFields.contains(fieldName);
    }
}