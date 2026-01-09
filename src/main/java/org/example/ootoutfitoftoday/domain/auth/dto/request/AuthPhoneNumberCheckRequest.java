package org.example.ootoutfitoftoday.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.example.ootoutfitoftoday.common.constant.ValidationRegex;

@Getter
@Setter
public class AuthPhoneNumberCheckRequest {

    @NotBlank(message = "휴대폰 번호는 필수 입력값입니다.")
    @Pattern(regexp = ValidationRegex.PHONE_NUMBER_REGEX, message = "휴대폰 번호는 하이픈(-) 없이 숫자만 입력해야 합니다.")
    private String value;
}
