package org.example.ootoutfitoftoday.domain.salepostimage.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.image.exception.ImageErrorCode;
import org.example.ootoutfitoftoday.domain.image.exception.ImageException;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SalePostImageCommandServiceImpl implements SalePostImageCommandService {

    private final SalePostImageRepository salePostImageRepository;
    private final SalePostQueryService salePostQueryService;
    private final ImageQueryService imageQueryService;

    // SalePostImage 생성
    // 설명: SalePost 생성 시 사용
    //      이미지 검증, SalePostImage 생성, 저장을 한 번에 처리
    @Override
    public List<SalePostImage> createSalePostImages(Long salePostId, List<Long> imageIds) {
        // 중복 검증
        if (imageIds.size() != new HashSet<>(imageIds).size()) {
            log.warn("판매글 이미지 중복 감지 - imageIds: {}", imageIds);
            throw new SalePostImageException(SalePostImageErrorCode.DUPLICATE_SALE_POST_IMAGE);
        }

        // 이미지 검증 및 조회(범용 Image 엔티티)
        List<Image> validatedImages = imageQueryService.findAllByIdInAndIsDeletedFalse(imageIds);

        // 개수 검증(중복 체크)
        if (validatedImages.size() != imageIds.size()) {
            log.warn("이미지 검증 실패 - 요청: {}, 조회됨: {}", imageIds.size(), validatedImages.size());
            throw new ImageException(ImageErrorCode.IMAGE_NOT_FOUND);
        }

        // SalePost 조회
        SalePost salePost = salePostQueryService.findSalePostById(salePostId);

        // SalePostImage 생성(중간 테이블)
        List<SalePostImage> salePostImages = new ArrayList<>();
        for (int i = 0; i < validatedImages.size(); i++) {
            boolean isMain = (i == 0);  // 첫 번째 이미지가 메인

            SalePostImage salePostImage = SalePostImage.create(
                    salePost,
                    validatedImages.get(i),
                    i,    // displayOrder
                    isMain
            );

            salePostImages.add(salePostImage);
        }

        // 일괄 저장
        return salePostImageRepository.saveAll(salePostImages);
    }

    // 이미지 추가
    @Override
    public List<SalePostImageResponse> addImages(
            Long salePostId,
            Long userId,
            List<Long> imageIds
    ) {
        // 권한 검증
        SalePost salePost = validateOwnership(salePostId, userId);

        // 중복 검증
        if (imageIds.size() != new HashSet<>(imageIds).size()) {
            log.warn("판매글 이미지 중복 감지 - imageIds: {}", imageIds);
            throw new SalePostImageException(SalePostImageErrorCode.DUPLICATE_SALE_POST_IMAGE);
        }

        // 이미지 검증 및 조회(범용 Image 엔티티)
        List<Image> validatedImages = imageQueryService.findAllByIdInAndIsDeletedFalse(imageIds);

        // 기존 이미지 개수 확인(중간 테이블 SalePostImage)
        List<SalePostImage> existingSalePostImages = salePostImageRepository.findBySalePostIdAndIsDeletedFalseWithoutImage(salePostId);

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
                    currentOrder + i,
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

        // 중복 검증
        if (imageIds.size() != new HashSet<>(imageIds).size()) {
            log.warn("판매글 이미지 중복 감지 - imageIds: {}", imageIds);
            throw new SalePostImageException(SalePostImageErrorCode.DUPLICATE_SALE_POST_IMAGE);
        }

        // 이미지 검증 및 조회
        List<Image> validatedImages = imageQueryService.findAllByIdInAndIsDeletedFalse(imageIds);

        // 비즈니스 검증: 최대 10개
        if (validatedImages.size() > 10) {
            log.warn("이미지 개수 초과 - salePostId: {}, 개수: {}", salePostId, validatedImages.size());
            throw new SalePostImageException(SalePostImageErrorCode.TOO_MANY_SALE_POST_IMAGES);
        }

        // 기존 이미지 모두 soft delete
        List<SalePostImage> existingSalePostImages = salePostImageRepository.findBySalePostIdAndIsDeletedFalseWithoutImage(salePostId);
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
                    i,    // displayOrder
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
        List<SalePostImage> allSalePostImages = salePostImageRepository.findBySalePostIdAndIsDeletedFalseWithoutImage(salePostId);
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
            List<SalePostImage> remainingSalePostImages = salePostImageRepository.findBySalePostIdAndIsDeletedFalseWithoutImage(salePostId);
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
        List<SalePostImage> allSalePostImages = salePostImageRepository.findBySalePostIdAndIsDeletedFalseWithoutImage(salePostId);

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
        validateOwnership(salePostId, userId);

        // 모든 이미지 조회
        List<SalePostImage> allSalePostImages = salePostImageRepository.findBySalePostIdAndIsDeletedFalseWithoutImage(salePostId);

        // 요청된 이미지 ID와 실제 이미지 ID가 일치하는지 확인
        Set<Long> existingImageIds = allSalePostImages.stream()
                .map(SalePostImage::getId)
                .collect(Collectors.toSet());
        Set<Long> requestedImageIds = new HashSet<>(orderedImageIds);

        if (allSalePostImages.size() != orderedImageIds.size() || !existingImageIds.equals(requestedImageIds)) {
            log.warn("이미지 순서 변경 요청 오류: 이미지 목록 불일치 - salePostId: {}, 기존: {}, 요청: {}", salePostId, existingImageIds, requestedImageIds);
            throw new SalePostImageException(SalePostImageErrorCode.SALE_POST_IMAGE_NOT_BELONG);
        }

        // ID로 매핑
        Map<Long, SalePostImage> salePostImageMap = allSalePostImages.stream()
                .collect(Collectors.toMap(SalePostImage::getId, salePostImage -> salePostImage));

        // 순서 업데이트
        for (int i = 0; i < orderedImageIds.size(); i++) {
            Long salePostImageId = orderedImageIds.get(i);
            SalePostImage salePostImage = salePostImageMap.get(salePostImageId);

            if (salePostImage == null) {
                log.warn("이미지를 찾을 수 없음 - salePostImageId: {}, salePostId: {}", salePostImageId, salePostId);
                throw new SalePostImageException(SalePostImageErrorCode.SALE_POST_IMAGE_NOT_FOUND);
            }

            // displayOrder만 업데이트(첫 번째가 자동으로 메인이 되도록 할지는 비즈니스 요구사항에 따라)
            salePostImage.updateDisplayOrder(i);

            // 옵션: 첫 번째 이미지를 자동으로 메인으로 설정하려면
            salePostImage.updateMain(i == 0);
        }

        // 일괄 저장 (변경 감지로 UPDATE 쿼리만 발생)
        salePostImageRepository.saveAll(allSalePostImages);
    }

    // 일괄 soft delete
    // 설명: SalePost 삭제 시 연관된 SalePostImage를 일괄 삭제
    // 변경 이유: 삭제 로직을 CommandService에 집중
    @Override
    public void bulkSoftDelete(List<SalePostImage> salePostImages) {
        for (SalePostImage salePostImage : salePostImages) {
            salePostImage.softDelete();
        }

        salePostImageRepository.saveAll(salePostImages);
    }

    // 권한 검증 헬퍼 메서드 (기존과 동일)
    private SalePost validateOwnership(Long salePostId, Long userId) {
        SalePost salePost = salePostQueryService.findSalePostById(salePostId);

        if (!salePost.isOwnedBy(userId)) {
            log.warn("권한 없는 접근 - salePostId: {}, userId: {}", salePostId, userId);
            throw new SalePostException(SalePostErrorCode.UNAUTHORIZED_ACCESS);
        }

        return salePost;
    }
}