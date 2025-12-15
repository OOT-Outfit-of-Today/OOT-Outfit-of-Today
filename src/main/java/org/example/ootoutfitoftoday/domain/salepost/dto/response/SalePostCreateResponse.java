package org.example.ootoutfitoftoday.domain.salepost.dto.response;

import com.ootcommon.salepost.enums.SaleStatus;
import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.common.util.Location;
import org.example.ootoutfitoftoday.common.util.PointFormatAndParse;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepostimage.dto.response.SalePostImageResponse;
import org.example.ootoutfitoftoday.domain.salepostimage.entity.SalePostImage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SalePostCreateResponse {

    private final Long salePostId;
    private final String title;
    private final String content;
    private final BigDecimal price;
    private final SaleStatus status;
    private final String tradeAddress;
    private final BigDecimal tradeLatitude;
    private final BigDecimal tradeLongitude;
    private final Long userId;
    private final Long categoryId;
    private final List<SalePostImageResponse> images;    // null 가능
    private final LocalDateTime createdAt;

    // 수정: SalePost만으로 생성(이미지 없이)
    // 설명: 판매글만 생성하고 이미지는 나중에 추가할 때 사용
    public static SalePostCreateResponse from(SalePost salePost) {
        Location location = PointFormatAndParse.parse(salePost.getTradeLocation());

        return SalePostCreateResponse.builder()
                .salePostId(salePost.getId())
                .title(salePost.getTitle())
                .content(salePost.getContent())
                .price(salePost.getPrice())
                .status(salePost.getStatus())
                .tradeAddress(salePost.getTradeAddress())
                .tradeLatitude(location.latitude())
                .tradeLongitude(location.longitude())
                .userId(salePost.getUser().getId())
                .categoryId(salePost.getCategory().getId())
                .images(null)    // 이미지 없음
                .createdAt(salePost.getCreatedAt())
                .build();
    }

    // 추가: SalePost + SalePostImage 리스트로 생성
    // 설명: 이미지 포함하여 Response 생성
    public static SalePostCreateResponse fromWithImages(SalePost salePost, List<SalePostImage> salePostImages
    ) {
        Location location = PointFormatAndParse.parse(salePost.getTradeLocation());

        return SalePostCreateResponse.builder()
                .salePostId(salePost.getId())
                .title(salePost.getTitle())
                .content(salePost.getContent())
                .price(salePost.getPrice())
                .status(salePost.getStatus())
                .tradeAddress(salePost.getTradeAddress())
                .tradeLatitude(location.latitude())
                .tradeLongitude(location.longitude())
                .images(salePostImages.stream()
                        .map(SalePostImageResponse::from)
                        .toList())
                .createdAt(salePost.getCreatedAt())
                .build();
    }
}