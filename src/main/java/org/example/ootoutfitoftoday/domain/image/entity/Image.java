package org.example.ootoutfitoftoday.domain.image.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "images")
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false)
    private String url;

    @Column(length = 255, nullable = false)
    private String fileName;

    @Column(length = 1000, nullable = false)
    private String s3Key;

    @Column(length = 100, nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImageType type;

    // 추가(선택사항): 파일 해시 필드
    // 설명: 같은 파일 업로드 시 중복 방지
    // 이유: 투명한 이미지 중복 제거(Phase 4에서 활용)
    //      SHA-256 해시로 동일 파일 감지
    @Column(length = 64, unique = true)
    private String fileHash;

    @Builder(access = AccessLevel.PROTECTED)
    public Image(
            String url,
            String fileName,
            String s3Key,
            String contentType,
            Long size,
            ImageType type,
            String fileHash
    ) {
        this.url = url;
        this.fileName = fileName;
        this.s3Key = s3Key;
        this.contentType = contentType;
        this.size = size;
        this.type = type;
        this.fileHash = fileHash;
    }

    public static Image create(
            String url,
            String fileName,
            String s3Key,
            String contentType,
            Long size,
            ImageType type,
            String fileHash
    ) {
        return Image.builder()
                .url(url)
                .fileName(fileName)
                .s3Key(s3Key)
                .contentType(contentType)
                .size(size)
                .type(type)
                .fileHash(fileHash)
                .build();
    }

    // 추가: 기존 create 메서드 오버로딩(하위 호환성)
    // 설명: 기존 코드와의 호환성을 위해 fileHash 없는 버전 유지
    public static Image create(
            String url,
            String fileName,
            String s3Key,
            String contentType,
            Long size,
            ImageType type
    ) {
        return create(url, fileName, s3Key, contentType, size, type, null);
    }
}