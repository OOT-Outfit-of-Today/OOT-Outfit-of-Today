package org.example.ootoutfitoftoday.domain.salepost.service.command;

import com.ootcommon.salepost.enums.SaleStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.util.PointFormatAndParse;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.category.service.query.CategoryQueryService;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostUpdateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostUpdateResponse;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.service.query.SalePostQueryService;
import org.example.ootoutfitoftoday.domain.salepostimage.entity.SalePostImage;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostException;
import org.example.ootoutfitoftoday.domain.salepost.repository.SalePostRepository;
import org.example.ootoutfitoftoday.domain.salepostimage.service.command.SalePostImageCommandService;
import org.example.ootoutfitoftoday.domain.salepostimage.service.query.SalePostImageQueryService;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SalePostCommandServiceImpl implements SalePostCommandService {

    private final UserQueryService userQueryService;
    private final CategoryQueryService categoryQueryService;
    private final SalePostQueryService salePostQueryService;
    private final SalePostRepository salePostRepository;
    private final EntityManager entityManager;
    private final SalePostImageCommandService salePostImageCommandService;
    private final SalePostImageQueryService salePostImageQueryService;

    // 수정: 판매글 생성(이미지 필수)
    // 설명: imageIds는 @NotEmpty로 검증됨(1~10개)
    //      SalePost + SalePostImage 함께 생성
    @Override
    @CacheEvict(value = "salePostListCache", allEntries = true)
    public SalePostCreateResponse createSalePost(Long userId, SalePostCreateRequest request) {

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        Category category = categoryQueryService.findById(request.getCategoryId());

        String tradeLocation = PointFormatAndParse.format(
                request.getTradeLatitude(),
                request.getTradeLongitude()
        );

        // 이미지 없이 SalePost 생성
        SalePost salePost = SalePost.create(
                user,
                category,
                request.getTitle(),
                request.getContent(),
                request.getPrice(),
                request.getTradeAddress(),
                tradeLocation
        );

        // Native Query로 SalePost 저장
        String status = salePost.getStatus().name();
        salePostRepository.saveAsNativeQuery(
                salePost.getTitle(),
                salePost.getContent(),
                salePost.getPrice(),
                status,
                salePost.getTradeAddress(),
                salePost.getTradeLocation(),
                salePost.getUser().getId(),
                salePost.getCategory().getId(),
                null,    // 일반 판매글 생성 시 recommendationId는 null(추천과 무관)
                false
        );

        Long salePostId = salePostRepository.findLastInsertId();

        // SalePostImageService로 위임
        // 설명: @NotEmpty로 검증되었으므로 무조건 1개 이상 존재
        List<SalePostImage> salePostImages = salePostImageCommandService.createSalePostImages(salePostId, request.getImageIds());

        SalePost savedSalePost = salePostRepository.findByIdAsNativeQuery(salePostId).orElseThrow(
                () -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        return SalePostCreateResponse.from(savedSalePost, salePostImages);
    }

    // Recommendation에서 판매글 생성
    @Override
    public SalePostCreateResponse createSalePostFromRecommendation(
            Recommendation recommendation,
            Long categoryId,
            String title,
            String content,
            BigDecimal price,
            String tradeAddress,
            BigDecimal tradeLatitude,
            BigDecimal tradeLongitude,
            List<Long> imageIds
    ) {
        // 방어적 검증: 이미지 필수
        if (imageIds == null || imageIds.isEmpty()) {
            log.error("추천에서 판매글 생성 시 이미지 필수 - recommendationId: {}", recommendation.getId());
            throw new SalePostException(SalePostErrorCode.EMPTY_IMAGES);
        }

        Category category = categoryQueryService.findById(categoryId);

        String tradeLocation = PointFormatAndParse.format(
                tradeLatitude,
                tradeLongitude
        );

        // SalePost 생성
        SalePost salePost = SalePost.createFromRecommendation(
                recommendation,
                category,
                title,
                content,
                price,
                tradeAddress,
                tradeLocation
        );

        // Native Query로 저장
        String status = salePost.getStatus().name();
        salePostRepository.saveAsNativeQuery(
                salePost.getTitle(),
                salePost.getContent(),
                salePost.getPrice(),
                status,
                salePost.getTradeAddress(),
                salePost.getTradeLocation(),
                salePost.getUser().getId(),
                salePost.getCategory().getId(),
                recommendation.getId(),
                false
        );

        Long salePostId = salePostRepository.findLastInsertId();

        // SalePostImage 생성
        List<SalePostImage> salePostImages = salePostImageCommandService.createSalePostImages(salePostId, imageIds);

        SalePost savedSalePost = salePostRepository.findByIdAsNativeQuery(salePostId).orElseThrow(
                () -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        return SalePostCreateResponse.from(savedSalePost, salePostImages);
    }

    // 수정: SalePost만 수정(이미지 제외)
    // 설명: 이미지 수정은 SalePostImageCommandService로 분리
    @Override
    @CacheEvict(value = "salePostListCache", allEntries = true)
    public SalePostUpdateResponse updateSalePost(
            Long salePostId,
            Long userId,
            SalePostUpdateRequest request
    ) {
        // 권한 검증 헬퍼 메서드로 변경
        // 설명: 조회 + 권한 체크를 한 번에 처리
        SalePost salePost = validateOwnership(salePostId, userId);

        // 상태 검증 헬퍼 메서드로 분리
        // 설명: 수정 가능한 상태인지 확인
        validateStatusForUpdate(salePost);

        categoryQueryService.findById(request.getCategoryId());

        String tradeLocation = PointFormatAndParse.format(request.getTradeLatitude(), request.getTradeLongitude());

        // SalePost 정보만 업데이트(이미지 제외)
        salePostRepository.updateAsNativeQuery(
                salePostId,
                request.getTitle(),
                request.getContent(),
                request.getPrice(),
                request.getCategoryId(),
                request.getTradeAddress(),
                tradeLocation
        );

        entityManager.clear();

        SalePost updatedSalePost = salePostRepository.findByIdAsNativeQuery(salePostId).orElseThrow(
                () -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        return SalePostUpdateResponse.from(updatedSalePost);
    }

    // 수정: SalePost + 연관된 SalePostImage 모두 soft delete
    // 설명: cascade 제거했으므로 명시적으로 처리
    @Override
    @CacheEvict(value = "salePostListCache", allEntries = true)
    public void deleteSalePost(Long salePostId, Long userId) {

        // 권한 검증 헬퍼 메서드로 변경
        // 설명: 조회 + 권한 체크를 한 번에 처리
        SalePost salePost = validateOwnership(salePostId, userId);

        // 상태 검증 헬퍼 메서드로 분리
        // 설명: 삭제 가능한 상태인지 확인
        validateStatusForDelete(salePost);

        // SalePost soft delete
        salePost.softDelete();

        // QueryService로 조회(Image URL 불필요 - WithoutImage 사용)
        // 설명: soft delete만 하므로 메타데이터만 필요, Image fetch join 불필요
        List<SalePostImage> salePostImages = salePostImageQueryService.findBySalePostIdWithoutImage(salePostId);

        // CommandService로 일괄 삭제 위임
        // 변경 이유: 삭제 로직은 CommandService의 책임
        salePostImageCommandService.bulkSoftDelete(salePostImages);
    }

    @Override
    @CacheEvict(value = "salePostListCache", allEntries = true)
    public SalePostUpdateResponse updateSaleStatus(
            Long salePostId,
            Long userId,
            SaleStatus newStatus
    ) {
        // 권한 검증 헬퍼 메서드로 변경
        // 설명: 조회 + 권한 체크를 한 번에 처리
        SalePost salePost = validateOwnership(salePostId, userId);

        salePost.updateStatus(newStatus);

        salePostRepository.flush();
        entityManager.clear();

        SalePost updatedSalePost = salePostRepository.findByIdAsNativeQuery(salePostId).orElseThrow(
                () -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        return SalePostUpdateResponse.from(updatedSalePost);
    }

    // 권한 검증 헬퍼 메서드 추가
    // 설명: Native Query로 User, Category를 함께 조회하여 Lazy Loading 문제 방지
    // 목적: 중복 코드 제거 및 일관된 권한 검증 로직 제공
    // 반환: 검증된 SalePost 엔티티
    private SalePost validateOwnership(Long salePostId, Long userId) {
        SalePost salePost = salePostQueryService.findSalePostById(salePostId);

        if (!salePost.isOwnedBy(userId)) {
            log.warn("권한 없는 접근 - salePostId: {}, userId: {}", salePostId, userId);
            throw new SalePostException(SalePostErrorCode.UNAUTHORIZED_ACCESS);
        }

        return salePost;
    }

    // 수정 가능 상태 검증 헬퍼 메서드 추가
    // 설명: 판매 중 상태(AVAILABLE)인지 확인
    // 목적: 예약/거래중/완료 상태의 판매글은 수정 불가
    private void validateStatusForUpdate(SalePost salePost) {
        if (salePost.getStatus() == SaleStatus.RESERVED ||
                salePost.getStatus() == SaleStatus.TRADING ||
                salePost.getStatus() == SaleStatus.COMPLETED) {
            log.warn("판매글 수정 불가 - salePostId: {}, status: {}", salePost.getId(), salePost.getStatus());
            throw new SalePostException(SalePostErrorCode.CANNOT_UPDATE_NON_SELLING_POST);
        }
    }

    // 삭제 가능 상태 검증 헬퍼 메서드 추가
    // 설명: 예약 상태가 아닌지 확인
    // 목적: 예약된 판매글은 삭제 불가
    private void validateStatusForDelete(SalePost salePost) {
        if (salePost.getStatus() == SaleStatus.RESERVED) {
            log.warn("예약된 판매글 삭제 불가 - salePostId: {}, status: {}", salePost.getId(), salePost.getStatus());
            throw new SalePostException(SalePostErrorCode.CANNOT_DELETE_RESERVED_POST);
        }
    }
}