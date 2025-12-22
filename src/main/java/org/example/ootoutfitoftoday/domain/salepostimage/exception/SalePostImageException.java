package org.example.ootoutfitoftoday.domain.salepostimage.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class SalePostImageException extends GlobalException {

    public SalePostImageException(SalePostImageErrorCode salePostImageErrorCode) {
        super(salePostImageErrorCode);
    }
}