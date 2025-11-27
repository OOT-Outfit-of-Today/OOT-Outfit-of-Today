package org.example.ootoutfitoftoday.domain.closet.controller;

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
            AuthUser authUser,
            ClosetRequest closetRequest
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
            AuthUser authUser,
            int page,
            int size,
            String sort,
            String direction
    ) {
        Long loginUserId = authUser.getUserId();

        Page<ClosetGetResponse> closetGetResponses = closetQueryService.getMyClosets(
                loginUserId,
                page,
                size,
                sort,
                direction
        );

        return PageResponse.success(closetGetResponses, ClosetSuccessCode.CLOSETS_GET_MY_OK);
    }

    @Override
    @GetMapping("/public")
    public ResponseEntity<PageResponse<ClosetGetResponse>> getPublicClosets(
            Long targetUserId,
            int page,
            int size,
            String sort,
            String direction
    ) {
        Page<ClosetGetResponse> closetGetPublicResponses = closetQueryService.getPublicClosets(
                targetUserId,
                page,
                size,
                sort,
                direction
        );

        return PageResponse.success(closetGetPublicResponses, ClosetSuccessCode.CLOSETS_GET_PUBLIC_OK);
    }

    @Override
    @GetMapping("/{closetId}")
    public ResponseEntity<Response<ClosetGetResponse>> getCloset(
            Long closetId
    ) {

        ClosetGetResponse closetGetResponse = closetQueryService.getCloset(closetId);

        return Response.success(closetGetResponse, ClosetSuccessCode.CLOSET_GET_OK);
    }

    @Override
    @PutMapping("/{closetId}")
    public ResponseEntity<Response<ClosetUpdateResponse>> updateCloset(
            AuthUser authUser,
            Long closetId,
            ClosetRequest closetRequest
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
            AuthUser authUser,
            Long closetId
    ) {

        ClosetDeleteResponse response = closetCommandService.deleteCloset(
                authUser.getUserId(),
                closetId
        );

        return Response.success(response, ClosetSuccessCode.CLOSET_DELETE_OK);
    }
}