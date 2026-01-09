package org.example.ootoutfitoftoday.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.ootoutfitoftoday.common.constant.ValidationRegex;

@Getter
@Setter
public class AuthLoginIdCheckRequest {

    @NotBlank(message = "아이디는 필수 입력값입니다.")
    @Size(min = 4, max = 15, message = "아이디는 4~15자 사이여야 합니다.")
    @Pattern(regexp = ValidationRegex.ID_REGEX, message = "아이디는 영문, 숫자, 언더바(_)만 사용할 수 있고 공백은 불가합니다.")
    private String value;
}
