package org.example.ootoutfitoftoday.domain.clothes.entity;

import com.ootcommon.clothes.enums.ClothesColor;
import com.ootcommon.clothes.enums.ClothesSize;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;
import org.example.ootoutfitoftoday.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "clothes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Clothes extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ClothesSize clothesSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ClothesColor clothesColor;

    @Column(length = 255, nullable = false)
    private String description;

    @Column(nullable = true)
    private LocalDateTime lastWornAt; // todo: 필드명 다시 고려해보기!

    @OneToMany(mappedBy = "clothes")
    private List<ClosetClothesLink> closetClothesLinks = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Clothes(
            Category category,
            User user,
            ClothesSize clothesSize,
            ClothesColor clothesColor,
            String description
    ) {
        this.category = category;
        this.user = user;
        this.clothesSize = clothesSize;
        this.clothesColor = clothesColor;
        this.description = description;
    }

    public static Clothes create(
            Category category,
            User user,
            ClothesSize clothesSize,
            ClothesColor clothesColor,
            String description
    ) {

        return Clothes.builder()
                .category(category)
                .user(user)
                .clothesSize(clothesSize)
                .clothesColor(clothesColor)
                .description(description)
                .build();
    }

    public void update(
            Category category,
            ClothesSize clothesSize,
            ClothesColor clothesColor,
            String description
    ) {
        this.category = category;
        this.clothesSize = clothesSize;
        this.clothesColor = clothesColor;
        this.description = description;
    }

    public void updateLastWornAt(LocalDateTime wornAt) {

        this.lastWornAt = wornAt;
    }
}
