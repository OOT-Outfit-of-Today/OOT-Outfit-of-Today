package org.example.ootoutfitoftoday.domain.chatroom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.common.response.SliceResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.chatroom.dto.request.ChatroomRequest;
import org.example.ootoutfitoftoday.domain.chatroom.dto.response.ChatroomResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "채팅방", description = "채팅방 API")
@SecurityRequirement(name = "bearerAuth")
public interface ChatroomController {

    @Operation(
            summary = "채팅방 생성",
            description = "회원이 채팅방을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "409", description = "이미 채팅방이 존재함")
            }
    )
    ResponseEntity<Response<Void>> createChatroom(
            ChatroomRequest chatroomRequest,
            AuthUser authUser
    );

    @Operation(
            summary = "채팅방 조회",
            description = "회원이 채팅방을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 페이지 또는 사이즈 파라미터"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    ResponseEntity<SliceResponse<ChatroomResponse>> getChatrooms(
            AuthUser authUser,
            int page,
            int size
    );

    @Operation(
            summary = "채팅방 삭제",
            description = "회원이 채팅방을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
            }
    )
    ResponseEntity<Response<Void>> deleteChatroom(
            AuthUser authUser,
            Long chatroomId
    );
}