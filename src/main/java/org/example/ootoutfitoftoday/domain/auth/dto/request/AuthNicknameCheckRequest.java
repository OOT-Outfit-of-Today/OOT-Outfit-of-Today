package org.example.ootoutfitoftoday.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.ootoutfitoftoday.common.constant.ValidationRegex;

@Getter
public class AuthNicknameCheckRequest {

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자 사이여야 합니다.")
    @Pattern(regexp = ValidationRegex.NICKNAME_REGEX, message = "닉네임 전후 공백은 불가합니다.")
    private String nickname;
}
