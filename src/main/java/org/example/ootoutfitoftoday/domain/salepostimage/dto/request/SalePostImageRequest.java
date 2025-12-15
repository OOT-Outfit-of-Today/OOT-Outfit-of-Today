package org.example.ootoutfitoftoday.domain.salepostimage.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class SalePostImageRequest {

    // 이미지 ID 리스트
    // 설명: 이미 저장된 Image ID를 받음
    // 검증: 최소 1개, 최대 10개
    @NotEmpty(message = "이미지는 최소 1개 이상 필요합니다")
    @Size(max = 10, message = "이미지는 최대 10개까지 가능합니다")
    private List<Long> imageIds;
}