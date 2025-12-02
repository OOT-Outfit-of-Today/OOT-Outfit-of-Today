package org.example.ootoutfitoftoday.domain.wearrecord.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.request.WearRecordCreateRequest;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordCreateResponse;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordGetMyResponse;
import org.example.ootoutfitoftoday.domain.wearrecord.exception.WearRecordSuccessCode;
import org.example.ootoutfitoftoday.domain.wearrecord.service.command.WearRecordCommandService;
import org.example.ootoutfitoftoday.domain.wearrecord.service.query.WearRecordQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/wear-records")
public class WearRecordControllerImpl implements WearRecordController {

    private final WearRecordCommandService wearRecordCommandService;
    private final WearRecordQueryService wearRecordQueryService;

    @Override
    @PostMapping
    public ResponseEntity<Response<WearRecordCreateResponse>> createWearRecord(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody WearRecordCreateRequest request
    ) {
        log.info("착용 기록 등록 요청 시작 - 사용자 ID: {}, 옷 ID: {}", authUser.getUserId(), request.clothesId());

        WearRecordCreateResponse response = wearRecordCommandService.createWearRecord(
                authUser.getUserId(),
                request
        );

        log.info("착용 기록 등록 완료 - 착용 기록 ID: {}", response.wearRecordId());

        return Response.success(response, WearRecordSuccessCode.WEAR_RECORD_CREATED);
    }

    @Override
    @GetMapping
    public ResponseEntity<PageResponse<WearRecordGetMyResponse>> getMyWearRecords(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "wornAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        log.info("내 착용 기록 리스트 조회 요청 - 사용자 ID: {}, 페이지: {}, 사이즈: {}, 정렬: {} {}",
                authUser.getUserId(), page, size, sort, direction);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sort)
        );

        Page<WearRecordGetMyResponse> responsePage = wearRecordQueryService.getMyWearRecords(
                authUser.getUserId(),
                pageable
        );

        log.info("내 착용 기록 리스트 조회 완료 - 총 {}건 중 {}건 반환",
                responsePage.getTotalElements(), responsePage.getNumberOfElements());

        return PageResponse.success(responsePage, WearRecordSuccessCode.WEAR_RECORDS_GET_OK);
    }
}