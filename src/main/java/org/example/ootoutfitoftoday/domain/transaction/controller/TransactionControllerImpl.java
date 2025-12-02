package org.example.ootoutfitoftoday.domain.transaction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.payment.exception.PaymentSuccessCode;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.TransactionConfirmRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.TransactionRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionAcceptResponse;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionCancelResponse;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionCompleteResponse;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionResponse;
import org.example.ootoutfitoftoday.domain.transaction.exception.TransactionSuccessCode;
import org.example.ootoutfitoftoday.domain.transaction.service.command.TransactionCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
public class TransactionControllerImpl implements TransactionController {

    private final TransactionCommandService transactionCommandService;

    @Override
    @PostMapping("/request")
    public ResponseEntity<Response<TransactionResponse>> requestTransaction(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody TransactionRequest request
    ) {
        TransactionResponse response = transactionCommandService.requestTransaction(
                authUser.getUserId(),
                request
        );

        return Response.success(response, TransactionSuccessCode.TRANSACTION_REQUESTED);
    }

    @Override
    @PostMapping("/{transactionId}/confirm")
    public ResponseEntity<Response<TransactionResponse>> confirmTransaction(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long transactionId,
            @Valid @RequestBody TransactionConfirmRequest request
    ) {
        TransactionResponse response = transactionCommandService.confirmTransaction(
                authUser.getUserId(),
                transactionId,
                request
        );

        return Response.success(response, PaymentSuccessCode.PAYMENT_APPROVED);
    }

    @Override
    @PostMapping("/{transactionId}/accept")
    public ResponseEntity<Response<TransactionAcceptResponse>> acceptTransaction(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long transactionId
    ) {
        TransactionAcceptResponse response = transactionCommandService.acceptTransaction(
                authUser.getUserId(),
                transactionId
        );

        return Response.success(response, TransactionSuccessCode.TRANSACTION_ACCEPTED);
    }

    @Override
    @PostMapping("/{transactionId}/complete")
    public ResponseEntity<Response<TransactionCompleteResponse>> completeTransaction(
            @PathVariable Long transactionId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        TransactionCompleteResponse response = transactionCommandService.completeTransaction(
                authUser.getUserId(),
                transactionId
        );

        return Response.success(response, TransactionSuccessCode.TRANSACTION_COMPLETED);
    }

    @Override
    @PostMapping("/{transactionId}/cancel-buyer")
    public ResponseEntity<Response<TransactionCancelResponse>> cancelByBuyer(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long transactionId
    ) {
        TransactionCancelResponse response = transactionCommandService.cancelByBuyer(
                authUser.getUserId(),
                transactionId
        );

        return Response.success(response, TransactionSuccessCode.TRANSACTION_CANCELLED);
    }
}