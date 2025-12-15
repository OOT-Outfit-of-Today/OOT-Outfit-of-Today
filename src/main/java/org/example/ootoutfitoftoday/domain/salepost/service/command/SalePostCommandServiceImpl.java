package org.example.ootoutfitoftoday.domain.salepost.service.command;

import com.ootcommon.salepost.enums.SaleStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.util.PointFormatAndParse;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.category.service.query.CategoryQueryService;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.image.exception.ImageErrorCode;
import org.example.ootoutfitoftoday.domain.image.exception.ImageException;
import org.example.ootoutfitoftoday.domain.image.service.query.ImageQueryService;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostUpdateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostDetailResponse;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepostimage.entity.SalePostImage;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostException;
import org.example.ootoutfitoftoday.domain.salepostimage.repository.SalePostImageRepository;
import org.example.ootoutfitoftoday.domain.salepost.repository.SalePostRepository;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SalePostCommandServiceImpl implements SalePostCommandService {

    private final UserQueryService userQueryService;
    private final CategoryQueryService categoryQueryService;
    private final SalePostRepository salePostRepository;
    private final EntityManager entityManager;
    private final ImageQueryService imageQueryService;
    private final SalePostImageRepository salePostImageRepository;

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
                null,    // 일반 판매글 생성 시 recommendationId는 null (추천과 무관)
                false
        );

        Long salePostId = salePostRepository.findLastInsertId();

        // SalePostImage 생성(필수)
        // 설명: @NotEmpty로 검증되었으므로 무조건 1개 이상 존재
        List<SalePostImage> salePostImages = createSalePostImages(salePostId, request.getImageIds());

        SalePost savedSalePost = salePostRepository.findByIdAsNativeQuery(salePostId).orElseThrow(
                () -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        return SalePostCreateResponse.fromWithImages(savedSalePost, salePostImages);
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
        List<SalePostImage> salePostImages = createSalePostImages(salePostId, imageIds);

        SalePost savedSalePost = salePostRepository.findByIdAsNativeQuery(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        return SalePostCreateResponse.fromWithImages(savedSalePost, salePostImages);
    }

    // 추가: SalePostImage 생성 헬퍼 메서드
    // 설명: SalePost 생성과 이미지 추가에서 재사용
    //      이미지 검증, SalePostImage 생성, 저장을 한 번에 처리
    private List<SalePostImage> createSalePostImages(Long salePostId, List<Long> imageIds) {

        // 이미지 검증 및 조회(범용 Image 엔티티)
        List<Image> validatedImages = imageQueryService.findAllByIdInAndIsDeletedFalse(imageIds);

        // 개수 검증(중복 체크)
        if (validatedImages.size() != imageIds.size()) {
            log.warn("이미지 검증 실패 - 요청: {}, 조회됨: {}", imageIds.size(), validatedImages.size());
            throw new ImageException(ImageErrorCode.IMAGE_NOT_FOUND);
        }

        // SalePost 조회
        SalePost salePost = salePostRepository.findByIdAsNativeQuery(salePostId).orElseThrow(
                () -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));


        // SalePostImage 생성(중간 테이블)
        List<SalePostImage> salePostImages = new ArrayList<>();
        for (int i = 0; i < validatedImages.size(); i++) {
            boolean isMain = (i == 0);  // 첫 번째 이미지가 메인

            SalePostImage salePostImage = SalePostImage.create(
                    salePost,
                    validatedImages.get(i),
                    i + 1,  // displayOrder
                    isMain
            );

            salePostImages.add(salePostImage);
        }

        // 일괄 저장
        return salePostImageRepository.saveAll(salePostImages);
    }

    // 수정: SalePost만 수정(이미지 제외)
    // 설명: 이미지 수정은 SalePostImageCommandService로 분리
    @Override
    @CacheEvict(value = "salePostListCache", allEntries = true)
    public SalePostDetailResponse updateSalePost(
            Long salePostId,
            Long userId,
            SalePostUpdateRequest request
    ) {
        // 권한 및 상태 검증
        SalePost salePost = salePostRepository.findByIdWithDetailsAndNotDeleted(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        if (!salePost.isOwnedBy(userId)) {
            log.warn("권한 없는 접근 - salePostId: {}, userId: {}", salePostId, userId);
            throw new SalePostException(SalePostErrorCode.UNAUTHORIZED_ACCESS);
        }

        if (salePost.getStatus() == SaleStatus.RESERVED ||
                salePost.getStatus() == SaleStatus.TRADING ||
                salePost.getStatus() == SaleStatus.COMPLETED
        ) {
            log.warn("판매글 수정 불가 - salePostId: {}, status: {}", salePostId, salePost.getStatus());
            throw new SalePostException(SalePostErrorCode.CANNOT_UPDATE_NON_SELLING_POST);
        }

        String tradeLocation = PointFormatAndParse.format(request.getTradeLatitude(), request.getTradeLongitude());

        log.info("tradeLocation: {}", tradeLocation);

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

        // 이미지와 함께 조회하여 반환
        SalePost updatedSalePost = salePostRepository.findByIdAsNativeQuery(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        List<SalePostImage> salePostImages = salePostImageRepository.findBySalePostIdWithImage(salePostId);

        return SalePostDetailResponse.fromWithImages(updatedSalePost, salePostImages);
    }

    // 수정: SalePost + 연관된 SalePostImage 모두 soft delete
    // 설명: cascade 제거했으므로 명시적으로 처리
    @Override
    @CacheEvict(value = "salePostListCache", allEntries = true)
    public void deleteSalePost(Long salePostId, Long userId) {
        SalePost salePost = salePostRepository.findByIdAndIsDeletedFalse(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        if (!salePost.isOwnedBy(userId)) {
            log.warn("권한 없는 삭제 시도 - salePostId: {}, userId: {}", salePostId, userId);
            throw new SalePostException(SalePostErrorCode.UNAUTHORIZED_ACCESS);
        }

        if (salePost.getStatus() == SaleStatus.RESERVED) {
            log.warn("예약된 판매글 삭제 불가 - salePostId: {}, status: {}", salePostId, salePost.getStatus());
            throw new SalePostException(SalePostErrorCode.CANNOT_DELETE_RESERVED_POST);
        }

        // SalePost soft delete
        salePost.softDelete();

        // 연관된 SalePostImage도 soft delete
        // 설명: cascade 제거했으므로 명시적으로 처리
        List<SalePostImage> salePostImages = salePostImageRepository.findBySalePostId(salePostId);
        for (SalePostImage salePostImage : salePostImages) {
            salePostImage.softDelete();
        }
        salePostImageRepository.saveAll(salePostImages);
    }

    @Override
    @CacheEvict(value = "salePostListCache", allEntries = true)
    public SalePostDetailResponse updateSaleStatus(
            Long salePostId,
            Long userId,
            SaleStatus newStatus
    ) {
        SalePost salePost = salePostRepository.findByIdWithDetailsAndNotDeleted(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        if (!salePost.isOwnedBy(userId)) {
            log.warn("권한 없는 상태 변경 시도 - salePostId: {}, userId: {}, 요청 상태: {}", salePostId, userId, newStatus);
            throw new SalePostException(SalePostErrorCode.UNAUTHORIZED_ACCESS);
        }

        salePost.updateStatus(newStatus);

        salePostRepository.flush();
        entityManager.clear();

        SalePost updatedSalePost = salePostRepository.findByIdAsNativeQuery(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));

        // 이미지와 함께 반환
        List<SalePostImage> salePostImages = salePostImageRepository.findBySalePostIdWithImage(salePostId);

        return SalePostDetailResponse.fromWithImages(updatedSalePost, salePostImages);
    }
}