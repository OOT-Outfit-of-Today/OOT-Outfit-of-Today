package org.example.ootoutfitoftoday.domain.clothesImage.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.util.List;

@Getter
public class ClothesImageRequest {

    // @NotNull과 @Positive를 같이 사용하면 -> 값은 필수이면서 0보다 커야한다라는 의미!(리스트 안 객체의 제약 조건)
    @NotEmpty(message = "imageIds는 최소 1개 이상 필요합니다.") // 리스트의 제약조건
    private List<@NotNull @Positive Long> imageIds;
}
