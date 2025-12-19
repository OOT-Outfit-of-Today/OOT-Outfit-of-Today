package org.example.ootoutfitoftoday.domain.wearrecord.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WearRecordErrorCode implements ErrorCode {

    DUPLICATE_WEAR_RECORD_SAME_DAY("DUPLICATE_WEAR_RECORD_SAME_DAY", HttpStatus.CONFLICT, "해당 날짜에 이미 이 옷의 착용 기록이 있습니다."),
    WORN_AT_IN_FUTURE("WORN_AT_IN_FUTURE", HttpStatus.BAD_REQUEST, "미래 시각으로는 착용 기록을 생성할 수 없습니다."),
    WORN_AT_OUT_OF_RANGE("WORN_AT_OUT_OF_RANGE", HttpStatus.BAD_REQUEST, "착용 시각은 현재 기준 한 달 이내만 선택할 수 있습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}