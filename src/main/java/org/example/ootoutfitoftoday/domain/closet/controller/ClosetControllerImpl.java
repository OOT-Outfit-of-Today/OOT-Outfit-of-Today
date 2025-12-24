package org.example.ootoutfitoftoday.domain.closet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetCreateResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetDeleteResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetUpdateResponse;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetSuccessCode;
import org.example.ootoutfitoftoday.domain.closet.service.command.ClosetCommandService;
import org.example.ootoutfitoftoday.domain.closet.service.query.ClosetQueryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/closets")
public class ClosetControllerImpl implements ClosetController {

    private final ClosetCommandService closetCommandService;
    private final ClosetQueryService closetQueryService;

    @Override
    @PostMapping
    public ResponseEntity<Response<ClosetCreateResponse>> createCloset(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ClosetRequest closetRequest
    ) {
        ClosetCreateResponse closetCreateResponse = closetCommandService.createCloset(
                authUser.getUserId(),
                closetRequest
        );

        return Response.success(closetCreateResponse, ClosetSuccessCode.CLOSET_CREATED);
    }

    // 로그인 유저의 옷장 조회
    @Override
    @GetMapping("/me")
    public ResponseEntity<PageResponse<ClosetGetResponse>> getMyClosets(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Long userId = authUser.getUserId();

        Page<ClosetGetResponse> closetGetResponses = closetQueryService.getMyClosets(
                userId,
                page,
                size,
                sort,
                direction
        );

        return PageResponse.success(closetGetResponses, ClosetSuccessCode.CLOSETS_GET_MY_OK);
    }

    // 공개 옷장 조회(비회원 가능)
    @Override
    @GetMapping("/public")
    public ResponseEntity<PageResponse<ClosetGetResponse>> getPublicClosets(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Page<ClosetGetResponse> closetGetPublicResponses = closetQueryService.getPublicClosets(
                userId,
                page,
                size,
                sort,
                direction
        );

        return PageResponse.success(closetGetPublicResponses, ClosetSuccessCode.CLOSETS_GET_PUBLIC_OK);
    }

    // 자신의 옷장 단건 조회
    @Override
    @GetMapping("/me/{closetId}")
    public ResponseEntity<Response<ClosetGetResponse>> getMyCloset(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long closetId
    ) {
        Long userId = authUser.getUserId();

        ClosetGetResponse closetGetResponse = closetQueryService.getMyCloset(userId, closetId);

        return Response.success(closetGetResponse, ClosetSuccessCode.CLOSET_GET_MY_OK);
    }

    // 공개 옷장 단건 조회
    @Override
    @GetMapping("/public/{closetId}")
    public ResponseEntity<Response<ClosetGetResponse>> getPublicCloset(@PathVariable Long closetId) {
        ClosetGetResponse closetGetResponse = closetQueryService.getPublicCloset(closetId);

        return Response.success(closetGetResponse, ClosetSuccessCode.CLOSET_GET_PUBLIC_OK);
    }

    // 자신의 옷장 수정
    @Override
    @PutMapping("/me/{closetId}")
    public ResponseEntity<Response<ClosetUpdateResponse>> updateCloset(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long closetId,
            @Valid @RequestBody ClosetRequest closetRequest
    ) {
        ClosetUpdateResponse closetUpdateResponse = closetCommandService.updateCloset(
                authUser.getUserId(),
                closetId,
                closetRequest
        );

        return Response.success(closetUpdateResponse, ClosetSuccessCode.CLOSET_UPDATE_OK);
    }

    @Override
    @DeleteMapping("/{closetId}")
    public ResponseEntity<Response<ClosetDeleteResponse>> deleteCloset(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long closetId
    ) {
        ClosetDeleteResponse response = closetCommandService.deleteCloset(
                authUser.getUserId(),
                closetId
        );

        return Response.success(response, ClosetSuccessCode.CLOSET_DELETE_OK);
    }
}