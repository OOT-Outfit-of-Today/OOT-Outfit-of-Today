package org.example.ootoutfitoftoday.domain.salepostimage.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.image.service.query.ImageQueryService;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostException;
import org.example.ootoutfitoftoday.domain.salepost.service.query.SalePostQueryService;
import org.example.ootoutfitoftoday.domain.salepostimage.dto.response.SalePostImageResponse;
import org.example.ootoutfitoftoday.domain.salepostimage.entity.SalePostImage;
import org.example.ootoutfitoftoday.domain.salepostimage.exception.SalePostImageErrorCode;
import org.example.ootoutfitoftoday.domain.salepostimage.exception.SalePostImageException;
import org.example.ootoutfitoftoday.domain.salepostimage.repository.SalePostImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SalePostImageCommandServiceImpl implements SalePostImageCommandService {

    private final SalePostImageRepository salePostImageRepository;
    private final SalePostQueryService salePostQueryService;
    private final ImageQueryService imageQueryService;

    // 이미지 추가
    @Override
    public List<SalePostImageResponse> addImages(
            Long salePostId,
            Long userId,
            List<Long> imageIds
    ) {
        // 권한 검증
        SalePost salePost = validateOwnership(salePostId, userId);

        // 이미지 검증 및 조회(범용 Image 엔티티)
        List<Image> validatedImages = imageQueryService.findAllByIdInAndIsDeletedFalse(imageIds);

        // 기존 이미지 개수 확인(중간 테이블 SalePostImage)
        List<SalePostImage> existingSalePostImages = salePostImageRepository.findBySalePostId(salePostId);

        // 비즈니스 검증: 최대 10개
        if (existingSalePostImages.size() + validatedImages.size() > 10) {
            log.warn("이미지 개수 초과 - salePostId: {}, 기존: {}, 추가: {}", salePostId, existingSalePostImages.size(), validatedImages.size());
            throw new SalePostImageException(SalePostImageErrorCode.TOO_MANY_SALE_POST_IMAGES);
        }

        int currentOrder = existingSalePostImages.size();
        boolean hasMainImage = existingSalePostImages.stream().anyMatch(SalePostImage::getIsMain);

        // newSalePostImages(중간 테이블)에 새 이미지 추가
        List<SalePostImage> newSalePostImages = new ArrayList<>();
        for (int i = 0; i < validatedImages.size(); i++) {
            boolean isMain = !hasMainImage && i == 0;

            SalePostImage salePostImage = SalePostImage.create(
                    salePost,
                    validatedImages.get(i),
                    currentOrder + i + 1,
                    isMain
            );
            newSalePostImages.add(salePostImage);
        }

        // 일괄 저장
        List<SalePostImage> savedSalePostImages = salePostImageRepository.saveAll(newSalePostImages);

        return savedSalePostImages.stream()
                .map(SalePostImageResponse::from)
                .toList();
    }

    // 이미지 전체 교체
    @Override
    public List<SalePostImageResponse> replaceImages(
            Long salePostId,
            Long userId,
            List<Long> imageIds
    ) {
        // 권한 검증
        SalePost salePost = validateOwnership(salePostId, userId);

        // 이미지 검증 및 조회
        List<Image> validatedImages = imageQueryService.findAllByIdInAndIsDeletedFalse(imageIds);

        // 비즈니스 검증: 최대 10개
        if (validatedImages.size() > 10) {
            log.warn("이미지 개수 초과 - salePostId: {}, 개수: {}", salePostId, validatedImages.size());
            throw new SalePostImageException(SalePostImageErrorCode.TOO_MANY_SALE_POST_IMAGES);
        }

        // 기존 이미지 모두 soft delete
        List<SalePostImage> existingSalePostImages = salePostImageRepository.findBySalePostId(salePostId);
        for (SalePostImage existingSalePostImage : existingSalePostImages) {
            existingSalePostImage.softDelete();
        }
        salePostImageRepository.saveAll(existingSalePostImages);

        // 새 이미지 생성
        List<SalePostImage> newSalePostImages = new ArrayList<>();
        for (int i = 0; i < validatedImages.size(); i++) {
            SalePostImage salePostImage = SalePostImage.create(
                    salePost,
                    validatedImages.get(i),
                    i + 1,
                    i == 0
            );
            newSalePostImages.add(salePostImage);
        }

        // 일괄 저장
        List<SalePostImage> savedSalePostImages = salePostImageRepository.saveAll(newSalePostImages);

        return savedSalePostImages.stream()
                .map(SalePostImageResponse::from)
                .toList();
    }

    // 이미지 개별 삭제(최소 1개 검증)
    @Override
    public void deleteImage(
            Long salePostId,
            Long userId,
            Long salePostImageId
    ) {
        // 권한 검증
        validateOwnership(salePostId, userId);

        // SalePostImage 조회
        SalePostImage targetSalePostImage = salePostImageRepository.findById(salePostImageId).orElseThrow(
                () -> new SalePostImageException(SalePostImageErrorCode.SALE_POST_IMAGE_NOT_FOUND));

        // 해당 판매글의 이미지인지 검증
        if (!targetSalePostImage.getSalePost().getId().equals(salePostId)) {
            log.warn("이미지가 판매글에 속하지 않음 - salePostImageId: {}, salePostId: {}", salePostImageId, salePostId);
            throw new SalePostImageException(SalePostImageErrorCode.SALE_POST_IMAGE_NOT_BELONG);
        }

        // 최소 1개 검증
        List<SalePostImage> allSalePostImages = salePostImageRepository.findBySalePostId(salePostId);
        if (allSalePostImages.size() <= 1) {
            log.warn("마지막 이미지 삭제 불가 - salePostId: {}, 이미지 개수: {}", salePostId, allSalePostImages.size());
            throw new SalePostImageException(SalePostImageErrorCode.CANNOT_DELETE_LAST_SALE_POST_IMAGE);
        }

        // soft delete
        targetSalePostImage.softDelete();
        salePostImageRepository.save(targetSalePostImage);

        // 삭제한 이미지가 메인이었다면 다른 이미지를 메인으로 설정
        if (targetSalePostImage.getIsMain()) {
            // salePostId로 다시 조회
            List<SalePostImage> remainingSalePostImages = salePostImageRepository.findBySalePostId(salePostId);
            if (!remainingSalePostImages.isEmpty()) {
                remainingSalePostImages.get(0).updateMain(true);
                salePostImageRepository.save(remainingSalePostImages.get(0));
            }
        }
    }

    // 메인 이미지 변경
    @Override
    public void updateMainImage(
            Long salePostId,
            Long userId,
            Long salePostImageId
    ) {
        // 권한 검증
        validateOwnership(salePostId, userId);

        // 모든 이미지 조회
        List<SalePostImage> allSalePostImages = salePostImageRepository.findBySalePostId(salePostId);

        // 선택한 이미지 찾기
        SalePostImage targetSalePostImage = allSalePostImages.stream()
                .filter(salePostImage -> salePostImage.getId().equals(salePostImageId))
                .findFirst()
                .orElseThrow(() -> new SalePostImageException(SalePostImageErrorCode.SALE_POST_IMAGE_NOT_FOUND));

        // 이미 메인 이미지인지 확인
        if (targetSalePostImage.getIsMain()) {
            log.info("이미 메인 이미지 - salePostImageId: {}, salePostId: {}", salePostImageId, salePostId);
            throw new SalePostImageException(SalePostImageErrorCode.ALREADY_MAIN_SALE_POST_IMAGE);
        }

        // 모든 이미지의 isMain을 false로
        for (SalePostImage salePostImage : allSalePostImages) {
            salePostImage.updateMain(false);
        }

        // 선택한 이미지만 isMain = true
        targetSalePostImage.updateMain(true);

        // 일괄 저장
        salePostImageRepository.saveAll(allSalePostImages);
    }

    // 이미지 순서 변경
    @Override
    public void reorderImages(
            Long salePostId,
            Long userId,
            List<Long> orderedImageIds
    ) {
        // 권한 검증
        SalePost salePost = validateOwnership(salePostId, userId);

        // 모든 이미지 조회
        List<SalePostImage> allSalePostImages = salePostImageRepository.findBySalePostId(salePostId);

        // ID로 매핑
        Map<Long, SalePostImage> salePostImageMap = allSalePostImages.stream()
                .collect(Collectors.toMap(SalePostImage::getId, salePostImage -> salePostImage));

        // 순서 변경(기존 이미지 soft delete)
        List<SalePostImage> reorderedSalePostImages = new ArrayList<>();
        for (Long salePostImageId : orderedImageIds) {
            SalePostImage salePostImage = salePostImageMap.get(salePostImageId);

            if (salePostImage == null) {
                log.warn("이미지를 찾을 수 없음 - salePostImageId: {}, salePostId: {}", salePostImageId, salePostId);
                throw new SalePostImageException(SalePostImageErrorCode.SALE_POST_IMAGE_NOT_FOUND);
            }

            salePostImage.softDelete();
            reorderedSalePostImages.add(salePostImage);
        }

        // 기존 이미지 soft delete
        salePostImageRepository.saveAll(reorderedSalePostImages);

        // 새로운 순서로 재생성
        List<SalePostImage> newSalePostImages = new ArrayList<>();
        for (int i = 0; i < orderedImageIds.size(); i++) {
            Long salePostImageId = orderedImageIds.get(i);
            SalePostImage originalSalePostImage = salePostImageMap.get(salePostImageId);

            SalePostImage newSalePostImage = SalePostImage.create(
                    salePost,
                    originalSalePostImage.getImage(),
                    i + 1,
                    originalSalePostImage.getIsMain()
            );
            newSalePostImages.add(newSalePostImage);
        }

        salePostImageRepository.saveAll(newSalePostImages);
    }

    // 권한 검증 헬퍼 메서드
    private SalePost validateOwnership(Long salePostId, Long userId) {

        SalePost salePost = salePostQueryService.findSalePostById(salePostId);

        if (!salePost.isOwnedBy(userId)) {
            log.warn("권한 없는 접근 - salePostId: {}, userId: {}", salePostId, userId);
            throw new SalePostException(SalePostErrorCode.UNAUTHORIZED_ACCESS);
        }

        return salePost;
    }
}