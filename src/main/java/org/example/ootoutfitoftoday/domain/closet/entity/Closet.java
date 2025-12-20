package org.example.ootoutfitoftoday.domain.closet.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.user.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "closets")
public class Closet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 255, nullable = true)
    private String description;

    @Column(nullable = false)
    private Boolean isPublic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", unique = true)
    private Image image;

    @OneToMany(mappedBy = "closet")
    private List<ClosetClothesLink> closetClothesLinks = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Closet(
            User user,
            String name,
            String description,
            Boolean isPublic,
            Image image
    ) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.image = image;
    }

    public static Closet create(
            User user,
            String name,
            String description,
            Boolean isPublic
    ) {

        return Closet.builder()
                .user(user)
                .name(name)
                .description(description)
                .isPublic(isPublic)
                .build();
    }

    public void changeImage(Image image) {
        this.image = image;
    }

    public void update(
            String name,
            String description,
            Boolean isPublic
    ) {
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
    }

    public Long getUserId() {

        return this.user.getId();
    }

    public String getImageUrl() {

        return Optional.ofNullable(this.image)
                .map(Image::getUrl)
                .orElse(null);
    }
}