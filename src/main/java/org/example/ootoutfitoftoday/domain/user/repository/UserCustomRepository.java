package org.example.ootoutfitoftoday.domain.user.repository;

import org.example.ootoutfitoftoday.domain.user.dto.UserDuplicateCheckResult;

import java.time.LocalDateTime;

public interface UserCustomRepository {

    void bulkSoftDeleteUserRelatedData(
            Long id,
            LocalDateTime deletedAt
    );

    UserDuplicateCheckResult checkDuplicates(
            String loginId,
            String email,
            String nickname,
            String phoneNumber
    );
}