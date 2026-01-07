package org.example.ootoutfitoftoday.domain.auth.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

import java.util.List;

public class AuthException extends GlobalException {

    public AuthException(AuthErrorCode authErrorCode) {
        super(authErrorCode);
    }

    public AuthException(AuthErrorCode authErrorCode, AuthSuccessCode authSuccessCode) {
        super(authErrorCode, authSuccessCode);
    }

    // 중복 필드 전달
    public AuthException(AuthErrorCode authErrorCode, List<String> duplicateFields) {
        super(authErrorCode, duplicateFields);
    }
}