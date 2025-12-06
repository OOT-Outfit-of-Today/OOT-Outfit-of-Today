package org.example.ootoutfitoftoday.domain.closetclotheslink.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class ClosetClothesLinkException extends GlobalException {

    public ClosetClothesLinkException(ClosetClothesLinkErrorCode errorCode) {
        super(errorCode);
    }
}