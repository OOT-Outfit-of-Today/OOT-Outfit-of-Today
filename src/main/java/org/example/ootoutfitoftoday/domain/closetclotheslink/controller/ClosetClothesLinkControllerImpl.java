package org.example.ootoutfitoftoday.domain.closetclotheslink.controller;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.request.ClosetClothesLinkRequest;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkDeleteResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkGetResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.exception.ClosetClothesLinkSuccessCode;
import org.example.ootoutfitoftoday.domain.closetclotheslink.service.command.ClosetClothesLinkCommandService;
import org.example.ootoutfitoftoday.domain.closetclotheslink.service.query.ClosetClothesLinkQueryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/closets/{closetId}/clothes")
public class ClosetClothesLinkControllerImpl implements ClosetClothesLinkController {

    private final ClosetClothesLinkCommandService closetClothesLinkCommandService;
    private final ClosetClothesLinkQueryService closetClothesLinkQueryService;

    @Override
    @PostMapping
    public ResponseEntity<Response<ClosetClothesLinkResponse>> createClosetClothesLink(
            AuthUser authUser,
            Long closetId,
            ClosetClothesLinkRequest closetClothesLinkRequest
    ) {
        ClosetClothesLinkResponse closetClothesLinkResponse = closetClothesLinkCommandService.createClosetClothesLink(
                authUser.getUserId(),
                closetId,
                closetClothesLinkRequest
        );

        return Response.success(closetClothesLinkResponse, ClosetClothesLinkSuccessCode.CLOSET_CLOTHES_LINKED);
    }

    @Override
    @GetMapping
    public ResponseEntity<PageResponse<ClosetClothesLinkGetResponse>> getClosetClothesLink(
            AuthUser authUser,
            Long closetId,
            int page,
            int size,
            String sort,
            String direction
    ) {
        Page<ClosetClothesLinkGetResponse> closetClothesLinkGetResponses = closetClothesLinkQueryService.getClothesInCloset(
                authUser.getUserId(),
                closetId,
                page,
                size,
                sort,
                direction
        );

        return PageResponse.success(closetClothesLinkGetResponses, ClosetClothesLinkSuccessCode.CLOSET_CLOTHES_LIST_OK);
    }

    @Override
    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Response<ClosetClothesLinkDeleteResponse>> deleteClosetClothesLink(
            AuthUser authUser,
            Long closetId,
            Long clothesId
    ) {
        ClosetClothesLinkDeleteResponse closetClothesLinkDeleteResponse = closetClothesLinkCommandService.deleteClosetClothesLink(
                authUser.getUserId(),
                closetId,
                clothesId
        );

        return Response.success(closetClothesLinkDeleteResponse, ClosetClothesLinkSuccessCode.CLOSET_CLOTHES_DELETED);
    }
}