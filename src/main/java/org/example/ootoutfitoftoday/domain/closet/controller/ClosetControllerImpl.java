package org.example.ootoutfitoftoday.domain.closet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetCreateRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetUpdateRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.*;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetSuccessCode;
import org.example.ootoutfitoftoday.domain.closet.service.command.ClosetCommandService;
import org.example.ootoutfitoftoday.domain.closet.service.query.ClosetQueryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/closets")
public class ClosetControllerImpl implements ClosetController {

    private final ClosetCommandService closetCommandService;
    private final ClosetQueryService closetQueryService;

    @Override
    @PostMapping
    public ResponseEntity<Response<ClosetCreateResponse>> createCloset(
            AuthUser authUser,
            ClosetCreateRequest closetCreateRequest
    ) {
        log.info("옷장 생성 요청 - 사용자: {}, 이름: {}", authUser.getUserId(), closetCreateRequest.name());
        log.debug("옷장 생성 상세 - 공개여부: {}, 이미지ID: {}",
                closetCreateRequest.isPublic(), closetCreateRequest.imageId());

        ClosetCreateResponse closetCreateResponse = closetCommandService.createCloset(
                authUser.getUserId(),
                closetCreateRequest
        );

        log.info("옷장 생성 완료 - 옷장ID: {}, 사용자: {}",
                closetCreateResponse.closetId(), authUser.getUserId());

        return Response.success(closetCreateResponse, ClosetSuccessCode.CLOSET_CREATED);
    }

    @Override
    @GetMapping("/public")
    public ResponseEntity<PageResponse<ClosetGetPublicResponse>> getPublicClosets(
            int page,
            int size,
            String sort,
            String direction
    ) {
        log.info("공개 옷장 목록 조회 요청 - 페이지: {}, 크기: {}, 정렬: {}, 방향: {}", page, size, sort, direction);

        Page<ClosetGetPublicResponse> closetGetPublicResponses = closetQueryService.getPublicClosets(
                page,
                size,
                sort,
                direction
        );

        log.info("공개 옷장 목록 조회 완료 - 조회 건수: {}, 전체 건수: {}, 전체 페이지: {}",
                closetGetPublicResponses.getContent().size(),
                closetGetPublicResponses.getTotalElements(),
                closetGetPublicResponses.getTotalPages());

        return PageResponse.success(closetGetPublicResponses, ClosetSuccessCode.CLOSETS_GET_PUBLIC_OK);
    }

    @Override
    @GetMapping("/{closetId}")
    public ResponseEntity<Response<ClosetGetResponse>> getCloset(
            Long closetId
    ) {
        log.info("옷장 상세 조회 요청 - 옷장ID: {}", closetId);

        ClosetGetResponse closetGetResponse = closetQueryService.getCloset(closetId);

        log.debug("옷장 조회 완료 - 옷장ID: {}, 이름: {}, 공개여부: {}",
                closetId, closetGetResponse.name(), closetGetResponse.isPublic());

        return Response.success(closetGetResponse, ClosetSuccessCode.CLOSET_GET_OK);
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<PageResponse<ClosetGetMyResponse>> getClosetByMe(
            AuthUser authUser,
            int page,
            int size,
            String sort,
            String direction
    ) {
        log.info("내 옷장 목록 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}, 정렬: {}, 방향: {}",
                authUser.getUserId(), page, size, sort, direction);

        Page<ClosetGetMyResponse> closetGetMyResponses = closetQueryService.getMyClosets(
                authUser.getUserId(),
                page,
                size,
                sort,
                direction
        );

        log.info("내 옷장 목록 조회 완료 - 조회 건수: {}, 사용자: {}, 전체 건수: {}",
                closetGetMyResponses.getContent().size(), authUser.getUserId(),
                closetGetMyResponses.getTotalElements());

        return PageResponse.success(closetGetMyResponses, ClosetSuccessCode.CLOSETS_GET_MY_OK);
    }

    @Override
    @PutMapping("/{closetId}")
    public ResponseEntity<Response<ClosetUpdateResponse>> updateCloset(
            AuthUser authUser,
            Long closetId,
            ClosetUpdateRequest closetUpdateRequest
    ) {
        log.info("옷장 수정 요청 - 옷장ID: {}, 사용자: {}", closetId, authUser.getUserId());
        log.debug("수정 요청 상세 - 이름: {}, 공개여부: {}, 이미지ID: {}",
                closetUpdateRequest.name(), closetUpdateRequest.isPublic(), closetUpdateRequest.imageId());

        ClosetUpdateResponse closetUpdateResponse = closetCommandService.updateCloset(
                authUser.getUserId(),
                closetId,
                closetUpdateRequest
        );

        log.info("옷장 수정 완료 - 옷장ID: {}", closetId);

        return Response.success(closetUpdateResponse, ClosetSuccessCode.CLOSET_UPDATE_OK);
    }

    @Override
    @DeleteMapping("/{closetId}")
    public ResponseEntity<Response<ClosetDeleteResponse>> deleteCloset(
            AuthUser authUser,
            Long closetId
    ) {
        log.info("옷장 삭제 요청 - 옷장ID: {}, 사용자: {}", closetId, authUser.getUserId());

        ClosetDeleteResponse response = closetCommandService.deleteCloset(
                authUser.getUserId(),
                closetId
        );

        log.info("옷장 삭제 완료 - 옷장ID: {}, 삭제시간: {}",
                response.closetId(), response.deletedAt());

        return Response.success(response, ClosetSuccessCode.CLOSET_DELETE_OK);
    }
}