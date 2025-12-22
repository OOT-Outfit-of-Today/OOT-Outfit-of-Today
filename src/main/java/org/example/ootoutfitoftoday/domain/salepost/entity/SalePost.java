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

        return salePost;
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new SalePostException(SalePostErrorCode.INVALID_PRICE);
        }
    }

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