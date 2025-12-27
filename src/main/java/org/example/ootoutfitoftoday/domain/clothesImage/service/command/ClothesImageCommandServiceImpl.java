package org.example.ootoutfitoftoday.domain.clothesImage.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.example.ootoutfitoftoday.domain.clothesImage.dto.request.ClothesImageRequest;
import org.example.ootoutfitoftoday.domain.clothesImage.dto.response.ClothesImageChangeMainResponse;
import org.example.ootoutfitoftoday.domain.clothesImage.dto.response.ClothesImageLinkResponse;
import org.example.ootoutfitoftoday.domain.clothesImage.dto.response.ClothesImageResponse;
import org.example.ootoutfitoftoday.domain.clothesImage.entity.ClothesImage;
import org.example.ootoutfitoftoday.domain.clothesImage.exception.ClothesImageErrorCode;
import org.example.ootoutfitoftoday.domain.clothesImage.exception.ClothesImageException;
import org.example.ootoutfitoftoday.domain.clothesImage.repository.ClothesImageRepository;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.image.service.query.ImageQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ClothesImageCommandServiceImpl implements ClothesImageCommandService {

    private final ClothesImageRepository clothesImageRepository;
    private final ImageQueryService imageQueryService;
    private final ClothesQueryService clothesQueryService;

    /**
     * 이미지 등록
     * 1) 요청에서 null/중복 제거(순서 유지)
     * 2) 이미 다른 옷과 연결된 이미지가 있으면 예외
     * 3) 존재하지 않는 이미지 id가 있으면 예외
     * 4) 기존 메인 이미지가 존재한다면 메인 상태 유지
     * 5) 메인 이미지가 없다면 이번 요청의 첫 번째 이미지를 메인(main=true)으로 지정
     * ---
     * 1. set을 사용하는 이유?
     * -> 중복 제거 + 빠른 contains() 용도
     * - list.contains()는 평균적으로 O(n) 리스트를 끝까지 찾음
     * - Set.contains()는 평균적으로 O(1) 해시 기반
     * - 언제 사용하면 좋냐?
     * -> 이미 등록된 값인지 검사를 여러 번 해야 할 때 or 중복을 허용하지 않는 ID 집합을 만들어 필터링할 때, 권한 체크, 차단 목록 등
     * ---
     * 2. Map을 사용하는 이유?
     * -> 객체 빠른 조회(인덱싱) 용도
     * - list에서 매번 find하면 O(n) 반복이지만, Map으로 구현하면 O(1)로 꺼낼 수 있음.
     * - 요청으로 받은 ids를 기반으로 id를 찾기 때문에 Map이 최적임.
     * - 언제 사용하면 좋냐?
     * -> 요청으로 id 목록을 받고, 그 id에 해당하는 엔티티 혹은 dto를 반복적으로 꺼낼 때 혹은 FK 매핑, 중간테이블 생성, batch insert 같은 작업할 때
     * ---
     * 3. Objects.equals(a,b) - NPE 방지 비교
     * - 둘 중 하나가 null이어도 안전하게 비교할 수 있음.
     */
    @Override
    public ClothesImageLinkResponse saveClothesImages(Long userId, Long clothesId, ClothesImageRequest clothesImageRequest) {

        // 입력 정리(null 제거 + 중복 제거 + 순서 유지)
        List<Long> normalizedIds = normalizeAndValidateImageIds(clothesImageRequest.getImageIds());

        // 유저의 옷인지 검증과 함께 옷 객체 생성
        Clothes clothes = clothesQueryService.findClothesByIdAndUserIdAndIsDeletedFalse(userId, clothesId);

        // 이미지 재사용(다른 옷과 링크) 금지 정책 체크
        assertNotLinkedToOtherClothes(clothesId, normalizedIds);

        // DB에 실제로 존재하는 이미지인지 확인(없으면 예외)
        Map<Long, Image> imageMap = loadImageMapOrThrow(normalizedIds);

        // 현재 연결된 이미지들 조회(중복 연결 방지 + 메인 유지/결정에 사용)
        List<ClothesImage> existing = clothesImageRepository.findByClothesIdAndIsDeletedFalse(clothesId);

        // 이미 이 옷에 연결된 이미지 id 목록
        Set<Long> alreadyLinkedIds = existing.stream()
                .map(ci -> ci.getImage().getId())
                .collect(Collectors.toSet());

        // 요청 중 이미 연결된 이미지는 신규 링크 생성 대상에서 제외(중복 row 방지)
        List<Long> newIds = normalizedIds.stream()
                .filter(id -> !alreadyLinkedIds.contains(id))
                .toList();

        // 요청은 왔지만 새로 연결할 이미지가 없는 경우
        if (newIds.isEmpty()) {
            throw new ClothesImageException(ClothesImageErrorCode.IMAGE_ALREADY_LINKED);
        }

        // 기존 메인 이미지가 있으면 유지한다.
        // (POST는 "추가"의 의미로 동작하고, 메인 변경은 PATCH에서만 수행)
        boolean hasExistingMain = existing.stream().anyMatch(ClothesImage::getIsMain);

        Long mainIdToApply = null;
        if (!hasExistingMain) {
            // 현재 메인이 없다면, 요청의 첫 번째 이미지를 메인으로 지정
            mainIdToApply = normalizedIds.get(0);

            // 혹시 남아있는 레코드 중 main=true가 있으면 정리(정합성 방어)
            for (ClothesImage ci : existing) {
                ci.updateMain(Objects.equals(ci.getImage().getId(), mainIdToApply));
            }
            clothesImageRepository.saveAll(existing);
        }

        // clothes_images 레코드 생성(신규로 연결되는 것만)
        List<ClothesImage> links = buildLinks(clothes, newIds, imageMap, mainIdToApply);

        if (!links.isEmpty()) {
            clothesImageRepository.saveAll(links);
        }

        // 최종 연결된 이미지들 (기존 + 신규)
        List<ClothesImage> resultImages = new ArrayList<>();
        resultImages.addAll(existing);
        resultImages.addAll(links);

        // DTO 변환
        List<ClothesImageResponse> imageResponses =
                resultImages.stream()
                        .map(ClothesImageResponse::from)
                        .toList();

        return ClothesImageLinkResponse.from(
                userId,
                clothesId,
                imageResponses
        );
    }

    /**
     * 메인 이미지 변경
     * - 지정한 clothesImageId만 main=true로 설정한다.
     * - 나머지는 main=false로 설정한다.
     */
    @Override
    public ClothesImageChangeMainResponse changeMainImage(Long userId, Long clothesId, Long clothesImageId) {

        // 사용자의 옷인지 검증
        clothesQueryService.findClothesByIdAndUserIdAndIsDeletedFalse(userId, clothesId);

        List<ClothesImage> images = clothesImageRepository.findByClothesIdAndIsDeletedFalse(clothesId);

        if (images.isEmpty()) {
            throw new ClothesImageException(ClothesImageErrorCode.CLOTHES_IMAGE_NOT_FOUND);
        }

        ClothesImage target = null;

        for (ClothesImage image : images) {
            if (Objects.equals(image.getId(), clothesImageId)) {
                image.updateMain(true);
                target = image;
            } else {
                image.updateMain(false);
            }
        }

        if (target == null) {
            throw new ClothesImageException(ClothesImageErrorCode.CLOTHES_IMAGE_NOT_FOUND);
        }

        clothesImageRepository.saveAll(images);

        ClothesImageResponse clothesImageResponse = ClothesImageResponse.from(target);

        return ClothesImageChangeMainResponse.from(userId, clothesId, clothesImageResponse);
    }

    /**
     * 이미지 연결 해제
     * - 특정 clothesId에서 특정 이미지들을 연결 해제(soft delete)한다.
     * - 만약 삭제되는 것 중 메인이 있었다면, 남은 이미지 중 하나를 차선 메인으로 변경한다.
     */
    @Override
    public void removeClothesImages(Long userId, Long clothesId, ClothesImageRequest clothesImageRequest) {

        clothesQueryService.findClothesByIdAndUserIdAndIsDeletedFalse(userId, clothesId); // 유저 검증로직

        List<Long> normalizedIds = normalizeAndValidateImageIds(clothesImageRequest.getImageIds());

        // 실제로 연결되어 있는(삭제되지 않은) 링크만 조회
        List<ClothesImage> linkedImages = clothesImageRepository.findByClothesIdAndImageIdsAndIsDeletedFalse(clothesId, normalizedIds);

        if (linkedImages.isEmpty()) {
            log.warn("removeClothesImages - 연관관계 없는 이미지 삭제 시도. clothesId={}, imageIds={}", clothesId, normalizedIds);
            throw new ClothesImageException(ClothesImageErrorCode.CLOTHES_IMAGE_NOT_FOUND);
        }

        // 삭제 대상 중 메인이 있었는지 확인
        boolean mainImageWillBeDeleted = linkedImages.stream().anyMatch(ClothesImage::getIsMain);

        // soft delete 처리
        linkedImages.forEach(ClothesImage::softDelete);
        clothesImageRepository.saveAll(linkedImages);

        // 메인이 삭제되었다면 차선 메인을 하나 지정
        if (mainImageWillBeDeleted) {
            promoteFallbackMainIfNeeded(clothesId);
        }
    }

    @Override
    public int softDeleteAllByClothesIdIsDeletedFalse(Long id) {

        return clothesImageRepository.softDeleteAllByClothesIdIsDeletedFalse(id);
    }

    /**
     * 요청 id 리스트
     * - null 제거
     * - 중복 제거
     * - 원래 요청 순서는 유지
     */
    private List<Long> normalizeAndValidateImageIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ClothesImageException(ClothesImageErrorCode.IMAGE_EMPTY);
        }

        List<Long> normalized = ids.stream()
                .filter(Objects::nonNull)
                .distinct() // 순서 유지하면서 중복 제거
                .toList();

        if (normalized.isEmpty()) {
            throw new ClothesImageException(ClothesImageErrorCode.IMAGE_EMPTY);
        }

        return normalized;
    }

    /**
     * 다른 옷과 이미 링크된 이미지가 요청에 포함되어 있는지 확인한다.
     * - 포함되어 있으면 예외를 던져서 재사용을 막는다.
     */
    private void assertNotLinkedToOtherClothes(Long clothesId, List<Long> imageIds) {
        if (clothesImageRepository.existsLinkedImages(clothesId, imageIds)) {
            throw new ClothesImageException(ClothesImageErrorCode.IMAGE_ALREADY_LINKED_TO_OTHER_CLOTHES);
        }
    }

    // Image 엔티티를 한 번에 조회하고, 요청 id 중 없는 것이 있으면 예외
    private Map<Long, Image> loadImageMapOrThrow(List<Long> ids) {
        List<Image> images = imageQueryService.findAllByIdInAndIsDeletedFalse(ids);
        Map<Long, Image> map = images.stream().collect(Collectors.toMap(Image::getId, img -> img));

        // 요청한 id 수와 조회된 이미지 수가 다르면 -> 없는 id가 포함된 것
        if (map.size() != ids.size()) {
            List<Long> missing = ids.stream().filter(id -> !map.containsKey(id)).toList();
            log.warn("clothes image link - 존재하지 않는 이미지 id 포함. missing={}", missing);
            throw new ClothesImageException(ClothesImageErrorCode.CLOTHES_IMAGE_NOT_FOUND);
        }

        return map;
    }

    /**
     * clothes_images(중간 테이블) 레코드를 만들어주는 유틸 메서드.
     * - mainId에 해당하는 이미지만 main=true
     */
    private List<ClothesImage> buildLinks(
            Clothes clothes,
            List<Long> ids,
            Map<Long, Image> imageMap,
            Long mainId
    ) {
        List<ClothesImage> links = new ArrayList<>();
        for (Long id : ids) {
            Image image = imageMap.get(id);
            boolean isMain = (mainId != null) && id.equals(mainId);
            links.add(ClothesImage.create(clothes, image, isMain));
        }
        return links;
    }

    /**
     * 메인이 삭제되어 현재 메인이 없는 상태가 되면, 남아있는 이미지 중 하나를 차선 메인으로 변경
     * - 생성일 기준 가장 오래된 이미지를 메인으로 지정
     */
    private void promoteFallbackMainIfNeeded(Long clothesId) {
        List<ClothesImage> remaining = clothesImageRepository
                .findByClothesIdAndIsDeletedFalse(clothesId);

        if (remaining.isEmpty()) {
            return; // 남은 이미지가 없으면 메인도 없음
        }

        // 남은 것들의 메인을 전부 해제
        remaining.forEach(img -> img.updateMain(false));

        // id(PK)가 가장 먼저 생성된 것을 메인으로 지정(가장 오래된 데이터)
        ClothesImage fallback = remaining.stream()
                .min(Comparator.comparing(ClothesImage::getId))
                .orElseThrow(); // isEmpty()로 이미 방어했으니 사실상 발생하지 않음.

        fallback.updateMain(true);

        // 나머지도 메인 해제 상태를 저장하기 위해 saveAll 사용
        // fallback 1개만 저장해도 되지만, 정합성을 위해 전체 저장
        clothesImageRepository.saveAll(remaining);
    }
}