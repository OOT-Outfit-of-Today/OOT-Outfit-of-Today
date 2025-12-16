package org.example.ootoutfitoftoday.domain.salepostimage.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.image.exception.ImageErrorCode;
import org.example.ootoutfitoftoday.domain.image.exception.ImageException;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostException;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sale_post_images")
public class SalePostImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제거: imageUrl 필드 삭제
    // 설명: URL만 저장하는 대신 Image 엔티티를 직접 참조하도록 변경
    // 이유: Image 메타데이터 활용, Image 재사용 가능, 데이터 정합성 보장

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean isMain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_post_id", nullable = false)
    private SalePost salePost;

    // 추가: Image 엔티티 직접 참조
    // 설명: imageUrl(String) 대신 Image 엔티티를 참조
    // 이유:
    // 1. Image 메타데이터 활용 가능 (파일명, 크기, 타입 등)
    // 2. 같은 Image를 여러 판매글에서 재사용 가능
    // 3. Image 변경 시 자동 반영 (데이터 정합성)
    // 4. 고아 데이터 방지 (Image 삭제 시 감지 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Builder(access = AccessLevel.PROTECTED)
    private SalePostImage(
            SalePost salePost,
            Image image,
            Integer displayOrder,
            Boolean isMain
    ) {
        this.salePost = salePost;
        this.image = image;
        this.displayOrder = displayOrder;
        this.isMain = isMain;
    }

    public static SalePostImage create(
            SalePost salePost,
            Image image,
            Integer displayOrder,
            Boolean isMain
    ) {
        validateSalePost(salePost);
        validateImage(image);

        return SalePostImage.builder()
                .salePost(salePost)
                .image(image)
                .displayOrder(displayOrder)
                .isMain(isMain)
                .build();
    }

    // 추가: SalePost 검증 메서드
    private static void validateSalePost(SalePost salePost) {
        if (salePost == null) {
            throw new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND);
        }
    }

    // 수정: 검증 메서드 개선
    // 이유: Image 엔티티를 받으므로 검증 강화
    private static void validateImage(Image image) {
        if (image == null) {
            throw new SalePostException(SalePostErrorCode.EMPTY_IMAGES);
        }
        if (image.getId() == null) {
            throw new ImageException(ImageErrorCode.IMAGE_NOT_SAVED);
        }
    }

    // 제거: setSalePost 메서드 삭제
    // 설명: 생성 시 SalePost를 받으므로 setter 불필요
    // 이유: 불변성 유지, 양방향 동기화 코드 제거

    public void updateMain(boolean isMain) {
        this.isMain = isMain;
    }

    // 추가: Image 정보 접근 편의 메서드
    // 설명: Image의 URL, 파일명 등에 쉽게 접근하기 위한 메서드
    // 이유: Service에서 image.getImage().getUrl() 대신 image.getImageUrl() 사용 가능
    public String getImageUrl() {
        return image != null ? image.getUrl() : null;
    }
}