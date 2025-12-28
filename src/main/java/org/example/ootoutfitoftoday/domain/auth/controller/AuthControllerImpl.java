package org.example.ootoutfitoftoday.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.dto.request.*;
import org.example.ootoutfitoftoday.domain.auth.dto.response.AuthLoginResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.response.DeviceInfoResponse;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthSuccessCode;
import org.example.ootoutfitoftoday.domain.auth.service.command.AuthCommandService;
import org.example.ootoutfitoftoday.domain.auth.service.query.AuthQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthControllerImpl implements AuthController {

    private final AuthCommandService authCommandService;
    private final AuthQueryService authQueryService;

    @Override
    @PostMapping("/signup")
    public ResponseEntity<Response<Void>> signup(@Valid @RequestBody AuthSignupRequest request) {
        authCommandService.signup(request);

        return Response.success(null, AuthSuccessCode.USER_SIGNUP);
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<Response<AuthLoginResponse>> login(
            @Valid @RequestBody AuthLoginRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthLoginResponse response = authCommandService.login(request, httpRequest);

        return Response.success(response, AuthSuccessCode.USER_LOGIN);
    }

    @Override
    @GetMapping("/devices")
    public ResponseEntity<Response<List<DeviceInfoResponse>>> getDevices(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String currentDeviceId
    ) {
        List<DeviceInfoResponse> devices = authQueryService.getDeviceList(authUser, currentDeviceId);

        return Response.success(devices, AuthSuccessCode.DEVICE_LIST_RETRIEVED);
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<Response<AuthLoginResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthLoginResponse response = authCommandService.refresh(request.getRefreshToken(), request.getDeviceId(), httpRequest);

        return Response.success(response, AuthSuccessCode.TOKEN_REFRESH);
    }

    @Override
    @PostMapping("/oauth2/token/exchange")
    public ResponseEntity<Response<AuthLoginResponse>> exchangeOAuthToken(
            @Valid @RequestBody TokenExchangeRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthLoginResponse response = authCommandService.exchangeOAuthToken(request.getCode(), request.getDeviceId(), request.getDeviceName(), httpRequest);

        return Response.success(response, AuthSuccessCode.TOKEN_EXCHANGE);
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String deviceId
    ) {
        authCommandService.logout(authUser, deviceId);

        return Response.success(null, AuthSuccessCode.USER_LOGOUT);
    }

    @Override
    @PostMapping("/logout/all")
    public ResponseEntity<Response<Void>> logoutAll(@AuthenticationPrincipal AuthUser authUser) {
        authCommandService.logoutAll(authUser);

        return Response.success(null, AuthSuccessCode.USER_LOGOUT);
    }

    @Override
    @DeleteMapping("/devices/{deviceId}")
    public ResponseEntity<Response<Void>> removeDevice(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String deviceId,
            @RequestParam String currentDeviceId
    ) {
        authCommandService.removeDevice(authUser, deviceId, currentDeviceId);

        return Response.success(null, AuthSuccessCode.DEVICE_REMOVED);
    }

    @Override
    @DeleteMapping("/withdraw")
    public ResponseEntity<Response<Void>> withdraw(
            @Valid @RequestBody AuthWithdrawRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        authCommandService.withdraw(request, authUser);

        return Response.success(null, AuthSuccessCode.USER_WITHDRAW);
    }
}