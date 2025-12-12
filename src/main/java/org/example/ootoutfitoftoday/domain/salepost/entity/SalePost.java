package org.example.ootoutfitoftoday.domain.salepost.entity;

import com.ootcommon.salepost.enums.SaleStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostException;
import org.example.ootoutfitoftoday.domain.user.entity.User;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sale_posts")
public class SalePost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SaleStatus status;

    @Column(nullable = false, length = 50)
    private String tradeAddress;

    @Column(nullable = false, columnDefinition = "POINT SRID 4326", updatable = false, insertable = false)
    private String tradeLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 제거: images 필드 제거(양방향 -> 단방향)
    // 설명: SalePost는 SalePostImage를 알 필요 없음
    // 이유:
    // 1. 단방향 연관관계로 단순화
    // 2. Cascade 제거하여 Soft Delete 완벽 제어
    // 3. Service에서 명시적으로 관리(디버깅 쉬움)
    // 4. 양방향 동기화 코드 불필요
    // @OneToMany(mappedBy = "salePost", cascade = CascadeType.ALL, orphanRemoval = true)
    // @OrderBy("displayOrder ASC")
    // @BatchSize(size = 100)
    // @Where(clause = "is_deleted = false")
    // private List<SalePostImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id")
    private Recommendation recommendation;

    @Builder(access = AccessLevel.PROTECTED)
    private SalePost(
            User user,
            Category category,
            String title,
            String content,
            BigDecimal price,
            SaleStatus status,
            String tradeAddress,
            String tradeLocation,
            Recommendation recommendation
    ) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.content = content;
        this.price = price;
        this.status = status;
        this.tradeAddress = tradeAddress;
        this.tradeLocation = tradeLocation;
        this.recommendation = recommendation;
    }

    // 수정: create 메서드에서 images 파라미터 제거
    // 설명: SalePost 생성과 이미지 추가를 분리
    // 이유: Service에서 별도로 SalePostImage를 생성하도록 변경
    //      엔티티는 자신의 생성에만 집중 (Single Responsibility)
    public static SalePost create(
            User user,
            Category category,
            String title,
            String content,
            BigDecimal price,
            String tradeAddress,
            String tradeLocation
    ) {
        validatePrice(price);

        SalePost salePost = SalePost.builder()
                .user(user)
                .category(category)
                .title(title)
                .content(content)
                .price(price)
                .status(SaleStatus.AVAILABLE)
                .tradeAddress(tradeAddress)
                .tradeLocation(tradeLocation)
                .build();

        // 제거: 이미지 추가 로직 제거
        // Service에서 별도로 SalePostImage.create() 호출
        // for (int i = 0; i < images.size(); i++) {
        //     boolean isMain = (i == 0);
        //     SalePostImage salePostImage = SalePostImage.create(
        //             images.get(i),
        //             i + 1,
        //             isMain
        //     );
        //     salePost.addImage(salePostImage);
        // }

        return salePost;
    }

    // 수정: createFromRecommendation도 동일하게 수정
    public static SalePost createFromRecommendation(
            Recommendation recommendation,
            Category category,
            String title,
            String content,
            BigDecimal price,
            String tradeAddress,
            String tradeLocation
    ) {
        validatePrice(price);

        SalePost salePost = SalePost.builder()
                .user(recommendation.getUser())
                .category(category)
                .title(title)
                .content(content)
                .price(price)
                .status(SaleStatus.AVAILABLE)
                .tradeAddress(tradeAddress)
                .tradeLocation(tradeLocation)
                .recommendation(recommendation)
                .build();

        // 제거: 이미지 추가 로직

        return salePost;
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new SalePostException(SalePostErrorCode.INVALID_PRICE);
        }
    }

    // 제거: validateImages 메서드
    // 설명: Service로 이동
    // 이유: 이미지 검증은 Service의 책임
    // private static void validateImages(List<Image> images) {
    //     if (images == null || images.isEmpty()) {
    //         throw new SalePostException(SalePostErrorCode.EMPTY_IMAGES);
    //     }
    //
    //     Set<Long> uniqueIds = images.stream()
    //             .map(Image::getId)
    //             .collect(Collectors.toSet());
    //
    //     if (uniqueIds.size() != images.size()) {
    //         throw new SalePostException(SalePostErrorCode.DUPLICATE_IMAGE);
    //     }
    // }

    // 제거: addImage 메서드
    // 설명: images 필드가 없으므로 불필요
    // 이유: 단방향이므로 SalePost는 SalePostImage를 모름
    // public void addImage(SalePostImage image) {
    //     this.images.add(image);
    //     image.setSalePost(this);
    // }

    // 수정: update 메서드에서 images 파라미터 제거
    // 설명: 이미지 업데이트는 Service에서 별도로 처리
    public void update(
            Category category,
            String title,
            String content,
            BigDecimal price,
            String tradeAddress,
            String tradeLocation
    ) {
        validatePrice(price);

        this.category = category;
        this.title = title;
        this.content = content;
        this.price = price;
        this.tradeAddress = tradeAddress;
        this.tradeLocation = tradeLocation;
    }

    // 제거: updateImages 메서드
    // 설명: Service로 이동
    // 이유: 이미지 업데이트는 Service의 책임
    // public void updateImages(List<Image> images) {
    //     validateImages(images);
    //     this.images.clear();
    //     for (int i = 0; i < images.size(); i++) {
    //         boolean isMain = (i == 0);
    //         SalePostImage salePostImage = SalePostImage.create(
    //                 images.get(i),
    //                 i + 1,
    //                 isMain
    //         );
    //         this.addImage(salePostImage);
    //     }
    // }

    public boolean isOwnedBy(Long userId) {

        return this.user != null && Objects.equals(this.user.getId(), userId);
    }

    public void updateStatus(SaleStatus newStatus) {
        this.status = newStatus;
    }

    public User getSeller() {

        return user;
    }
}