package org.example.ootoutfitoftoday.domain.user.service.query;

import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.enums.SocialProvider;
import org.example.ootoutfitoftoday.domain.user.dto.UserCacheDto;
import org.example.ootoutfitoftoday.domain.user.dto.UserDuplicateCheckResult;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserPasswordVerificationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserGetMyInfoResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserQueryService {

    // 기존 개발 중복 체크 메서드(하위 호환성 유지)
    // TODO: no usages면 삭제
    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    // 회원가입 시 필요한 모든 필드의 중복 여부를 한 번에 확인
    // TODO: 캐시 도입 고려
    UserDuplicateCheckResult checkDuplicatesForSignup(
            String loginId,
            String email,
            String nickname,
            String phoneNumber
    );

    User findByLoginIdAndIsDeletedFalse(String loginId);

    User findByIdAndIsDeletedFalse(Long id);

    User findByEmailAndIsDeletedFalse(String email);

    Optional<User> findBySocialProviderAndSocialId(SocialProvider provider, String socialId);

    UserGetMyInfoResponse getMyInfo(Long userId);

    void verifyPassword(UserPasswordVerificationRequest request, AuthUser authUser);

    int countAllUsers();

    int countByIsDeleted(Boolean isDeleted);

    int countUsersRegisteredSince(LocalDateTime start, LocalDateTime end);

    User findByIdAsNativeQuery(Long id);

    Page<Long> findAllActiveUserIds(Pageable pageable);

    UserCacheDto findCachedByLoginId(String loginId);

    UserCacheDto findCachedById(Long id);

    UserCacheDto findCachedByEmail(String email);
}