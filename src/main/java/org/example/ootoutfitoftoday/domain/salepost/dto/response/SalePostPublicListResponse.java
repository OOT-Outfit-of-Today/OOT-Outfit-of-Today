package org.example.ootoutfitoftoday.domain.salepost.dto.response;

import com.ootcommon.salepost.enums.SaleStatus;
import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.common.util.Location;
import org.example.ootoutfitoftoday.common.util.PointFormatAndParse;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SalePostPublicListResponse {

    private final Long salePostId;
    private final String title;
    private final BigDecimal price;
    private final SaleStatus status;
    private final String tradeAddress;
    private final BigDecimal tradeLatitude;
    private final BigDecimal tradeLongitude;
    private final String thumbnailUrl;    // 외부에서 전달받음
    private final String sellerNickname;
    private final String categoryName;
    private final LocalDateTime createdAt;

    // 수정: thumbnailUrl을 파라미터로 받음
    // 설명: 단방향으로 수정됨(SalePost.images 제거)
    //      -> Service에서 별도 조회한 thumbnailUrl 전달받음
    public static SalePostListResponse from(SalePost salePost, String thumbnailUrl) {
        Location location = PointFormatAndParse.parse(salePost.getTradeLocation());

        return SalePostListResponse.builder()
                .salePostId(salePost.getId())
                .title(salePost.getTitle())
                .price(salePost.getPrice())
                .status(salePost.getStatus())
                .tradeAddress(salePost.getTradeAddress())
                .tradeLatitude(location.latitude())
                .tradeLongitude(location.longitude())
                .thumbnailUrl(thumbnailUrl)    // 외부에서 받은 값 사용
                .sellerNickname(salePost.getUser().getNickname())
                .categoryName(salePost.getCategory().getName())
                .createdAt(salePost.getCreatedAt())
                .build();
    }
}