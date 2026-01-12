package org.example.ootoutfitoftoday.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.example.ootoutfitoftoday.common.constant.ValidationRegex;

@Getter
public class AuthPhoneNumberCheckRequest {

    @NotBlank(message = "휴대폰 번호는 필수 입력값입니다.")
    @Size(min = 11, max = 11, message = "휴대폰 번호는 11자리여야 합니다.")
    @Pattern(regexp = ValidationRegex.PHONE_NUMBER_REGEX, message = "휴대폰 번호는 숫자만 입력해야 합니다.")
    private String phoneNumber;
}