package org.example.ootoutfitoftoday.common.constant;

public final class CacheNames {

    // 사용자 관련
    public static final String USER = "userCache";
    public static final String USER_EXISTS = "userExistsCache";
    // 판매글 관련
    public static final String SALE_POST_LIST = "salePostListCache";

    private CacheNames() {
        throw new AssertionError("상수 클래스를 인스턴스화할 수 없습니다.");
    }
    // 대시보드 관련은 package com.ootcommon.dashboard.constant 내 존재
}
