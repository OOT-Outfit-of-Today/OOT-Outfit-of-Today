package org.example.ootoutfitoftoday.domain.user.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.QChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.closet.entity.QCloset;
import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.QClosetClothesLink;
import org.example.ootoutfitoftoday.domain.clothes.entity.QClothes;
import org.example.ootoutfitoftoday.domain.salepost.entity.QSalePost;
import org.example.ootoutfitoftoday.domain.user.dto.UserDuplicateCheckResult;
import org.example.ootoutfitoftoday.domain.user.entity.QUser;
import org.example.ootoutfitoftoday.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    // TODO: 추가 필요
    @Override
    public void bulkSoftDeleteUserRelatedData(
            Long id,
            LocalDateTime deletedAt
    ) {
        QClothes clothes = QClothes.clothes;
        QCloset closet = QCloset.closet;
        QClosetClothesLink closetClothesLink = QClosetClothesLink.closetClothesLink;
        QSalePost salePost = QSalePost.salePost;
        QChatParticipatingUser chatParticipatingUser = QChatParticipatingUser.chatParticipatingUser;

        queryFactory.update(clothes)
                .set(clothes.isDeleted, true)
                .set(clothes.deletedAt, deletedAt)
                .where(clothes.user.id.eq(id), clothes.isDeleted.eq(false))
                .execute();

        queryFactory.update(closet)
                .set(closet.isDeleted, true)
                .set(closet.deletedAt, deletedAt)
                .where(closet.user.id.eq(id), closet.isDeleted.eq(false))
                .execute();

        queryFactory.update(closetClothesLink)
                .set(closetClothesLink.isDeleted, true)
                .set(closetClothesLink.deletedAt, deletedAt)
                .where(closetClothesLink.closet.user.id.eq(id), closetClothesLink.isDeleted.eq(false))
                .execute();

        queryFactory.update(salePost)
                .set(salePost.isDeleted, true)
                .set(salePost.deletedAt, deletedAt)
                .where(salePost.user.id.eq(id), salePost.isDeleted.eq(false))
                .execute();

        queryFactory.update(chatParticipatingUser)
                .set(chatParticipatingUser.isDeleted, true)
                .set(chatParticipatingUser.deletedAt, deletedAt)
                .where(chatParticipatingUser.user.id.eq(id), chatParticipatingUser.isDeleted.eq(false))
                .execute();

        em.clear();
    }

    @Override
    public UserDuplicateCheckResult checkDuplicates(
            String loginId,
            String email,
            String nickname,
            String phoneNumber
    ) {
        QUser user = QUser.user;

        // 각 필드별 중복 체크를 위한 OR 조건 생성
        BooleanExpression loginIdMatches = user.loginId.eq(loginId);
        BooleanExpression emailMatches = user.email.eq(email);
        BooleanExpression nicknameMatches = user.nickname.eq(nickname);
        BooleanExpression phoneNumberMatches = user.phoneNumber.eq(phoneNumber);

        // 삭제되지 않은 사용자만 검색
        BooleanExpression notDeletedUser = user.isDeleted.eq(false);

        // 중복된 사용자들을 모두 조회
        List<User> duplicateUsers = queryFactory
                .selectFrom(user)
                .where(
                        loginIdMatches
                                .or(emailMatches)
                                .or(nicknameMatches)
                                .or(phoneNumberMatches),
                        notDeletedUser
                )
                .fetch();

        // 중복이 없으면 즉시 반환(Early Return)
        if (duplicateUsers.isEmpty()) {
            return UserDuplicateCheckResult.noDuplicates();
        }

        // 중복된 필드명 수집
        List<String> duplicateFields = new ArrayList<>();

        for (User duplicateUser : duplicateUsers) {
            // 각 필드를 개별적으로 확인하여 중복 필드명 추가
            if (Objects.equals(duplicateUser.getLoginId(), loginId) && !duplicateFields.contains("loginId")) {
                duplicateFields.add("loginId");
            }
            if (Objects.equals(duplicateUser.getEmail(), email) && !duplicateFields.contains("email")) {
                duplicateFields.add("email");
            }

            if (Objects.equals(duplicateUser.getNickname(), nickname) && !duplicateFields.contains("nickname")) {
                duplicateFields.add("nickname");
            }
            if (Objects.equals(duplicateUser.getPhoneNumber(), phoneNumber) && !duplicateFields.contains("phoneNumber")) {
                duplicateFields.add("phoneNumber");
            }
        }

        return UserDuplicateCheckResult.withDuplicates(duplicateFields);
    }
}