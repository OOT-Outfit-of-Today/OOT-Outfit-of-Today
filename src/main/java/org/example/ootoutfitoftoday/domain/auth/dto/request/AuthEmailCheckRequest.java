package org.example.ootoutfitoftoday.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.ootoutfitoftoday.common.constant.ValidationRegex;

@Getter
@Setter
public class AuthEmailCheckRequest {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 50, message = "이메일은 최대 50자까지 가능합니다.")
    @Pattern(regexp = ValidationRegex.EMAIL_REGEX, message = "이메일 형식이 올바르지 않습니다.")
    private String email;
}
