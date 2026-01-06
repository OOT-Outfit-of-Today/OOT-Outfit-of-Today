package org.example.ootoutfitoftoday.domain.user.repository;

import com.querydsl.core.BooleanBuilder;
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
import java.util.*;

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

    /**
     * 회원가입 시 4개 필드의 중복 여부를 단일 쿼리로 확인
     * 1. 성능 최적화:
     *    - 기존: 4번의 DB 왕복(4 * network latency)
     *    - 개선: 1번의 DB 왕복(1 * network latency)
     *    - TODO 향상: 성능 개선 수치화
     * 2. 사용자 경험 개선:
     *    - 모든 중복 필드를 한 번에 확인
     *    - 여러 오류를 동시에 반환 가능
     * 동작 원리:
     * - BooleanBuilder로 동적 OR 조건 구성
     * - phoneNumber가 null이면 쿼리 조건에서 제외
     * - isDeleted = false로 삭제된 사용자 제외
     * - 조회된 사용자들의 필드를 개별 비교하여 중복 필드 수집
     */
    @Override
    public UserDuplicateCheckResult checkDuplicates(
            String loginId,
            String email,
            String nickname,
            String phoneNumber
    ) {
        QUser user = QUser.user;

        // BooleanBuilder로 각 필드별 중복 체크를 위한 동적 OR 조건 구성
        // phoneNumber는 null일 경우, 쿼리 조건에서 제외
        BooleanBuilder whereClause = new BooleanBuilder();
        whereClause.or(user.loginId.eq(loginId));
        whereClause.or(user.email.eq(email));
        whereClause.or(user.nickname.eq(nickname));

        // phoneNumber는 null이 아닌 경우, OR 조건에 추가
        // 이유: null끼리는 중복이 아니므로(소셜 로그인 사용자 고려)
        if (phoneNumber != null) {
            whereClause.or(user.phoneNumber.eq(phoneNumber));
        }

        // 중복 가능성이 있는 사용자들을 모두 조회
        List<User> duplicateUsers = queryFactory
                .selectFrom(user)
                .where(
                        whereClause,                     // OR 조건들
                        user.isDeleted.eq(false)    // AND 조건(삭제된 사용자 제외)
                )
                .fetch();

        // 중복이 없으면 즉시 반환(Early Return)
        if (duplicateUsers.isEmpty()) {
            return UserDuplicateCheckResult.noDuplicates();
        }

        // LinkedHashSet으로 중복된 필드명 수집
        // LinkedHashSet: 삽입 순서 유지 + 자동 중복 제거(add 호출 시점)
        // 이유: 제약조건(Entity, requestDto)가 이미 있지만, 방어적 + API 응답 순서 일관성
        Set<String> duplicateFieldsSet = new LinkedHashSet<>();

        // 각 사용자마다 어떤 필드가 중복인지 확인
        // 각 필드를 개별적으로 확인하여 중복 필드명 추가
        for (User duplicateUser : duplicateUsers) {
            if (Objects.equals(duplicateUser.getLoginId(), loginId)) {
                duplicateFieldsSet.add("loginId");
            }
            if (Objects.equals(duplicateUser.getEmail(), email)) {
                duplicateFieldsSet.add("email");
            }

            if (Objects.equals(duplicateUser.getNickname(), nickname)) {
                duplicateFieldsSet.add("nickname");
            }

            // phoneNumber는 입력값이 있을 때만 중복 체크
            // null끼리는 중복으로 판단하지 않음
            if (phoneNumber != null && Objects.equals(duplicateUser.getPhoneNumber(), phoneNumber)) {
                duplicateFieldsSet.add("phoneNumber");
            }
        }

        // Set을 List로 변환하여 반환
        // LinkedHashSet은 순서를 보장하므로 List 변환 후에도 순서 유지되나,
        // DTO는 List를 사용하는 것이 REST API 표준 관습
        return UserDuplicateCheckResult.withDuplicates(
                new ArrayList<>(duplicateFieldsSet)
        );
    }
}