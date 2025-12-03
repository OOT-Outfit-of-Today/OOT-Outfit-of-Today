package org.example.ootoutfitoftoday.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserPasswordVerificationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateInfoRequest;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateProfileImageRequest;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateTradeLocationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserGetMyInfoResponse;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserUpdateInfoResponse;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserUpdateProfileImageResponse;
import org.example.ootoutfitoftoday.domain.user.exception.UserSuccessCode;
import org.example.ootoutfitoftoday.domain.user.service.command.UserCommandService;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user/me")
public class UserControllerImpl implements UserController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    @Override
    @GetMapping
    public ResponseEntity<Response<UserGetMyInfoResponse>> getMyInfo(@AuthenticationPrincipal AuthUser authUser) {

        UserGetMyInfoResponse response = userQueryService.getMyInfo(authUser.getUserId());

        return Response.success(response, UserSuccessCode.GET_MY_INFO);
    }

    @Override
    @PostMapping("/password-verification")
    public ResponseEntity<Response<Void>> verifyPassword(
            @Valid @RequestBody UserPasswordVerificationRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        userQueryService.verifyPassword(request, authUser);

        return Response.success(null, UserSuccessCode.PASSWORD_VERIFIED);
    }

    @Override
    @PatchMapping
    public ResponseEntity<Response<UserUpdateInfoResponse>> updateInfo(
            @Valid @RequestBody UserUpdateInfoRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        UserUpdateInfoResponse response = userCommandService.updateInfo(request, authUser);

        return Response.success(response, UserSuccessCode.UPDATE_INFO);
    }

    @Override
    @PutMapping("/profile-image")
    public ResponseEntity<Response<UserUpdateProfileImageResponse>> updateProfileImage(
            @Valid @RequestBody UserUpdateProfileImageRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        UserUpdateProfileImageResponse response = userCommandService.updateProfileImage(authUser.getUserId(), request.getImageId()
        );

        return Response.success(response, UserSuccessCode.UPDATE_PROFILE_IMAGE);
    }

    @Override
    @DeleteMapping("/profile-image")
    public ResponseEntity<Response<Void>> deleteProfileImage(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        userCommandService.deleteProfileImage(authUser.getUserId());

        return Response.success(null, UserSuccessCode.DELETE_PROFILE_IMAGE);
    }

    @Override
    @PatchMapping("/locations")
    public ResponseEntity<Response<Void>> updateUserTradeLocation(
            @RequestBody UserUpdateTradeLocationRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        userCommandService.updateMyTradeLocation(request, authUser.getUserId());

        return Response.success(null, UserSuccessCode.UPDATED_TRADE_LOCATION);
    }
}