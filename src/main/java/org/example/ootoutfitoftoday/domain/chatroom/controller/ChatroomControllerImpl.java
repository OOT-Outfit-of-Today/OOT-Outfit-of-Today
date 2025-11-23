package org.example.ootoutfitoftoday.domain.chatroom.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.common.response.SliceResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.chatroom.dto.request.ChatroomRequest;
import org.example.ootoutfitoftoday.domain.chatroom.dto.response.ChatroomResponse;
import org.example.ootoutfitoftoday.domain.chatroom.exception.ChatroomSuccessCode;
import org.example.ootoutfitoftoday.domain.chatroom.service.command.ChatroomCommandService;
import org.example.ootoutfitoftoday.domain.chatroom.service.query.ChatroomQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/chatrooms")
@RequiredArgsConstructor
public class ChatroomControllerImpl implements ChatroomController {

    private final ChatroomCommandService chatroomCommandService;
    private final ChatroomQueryService chatroomQueryService;

    @Override
    @PostMapping
    public ResponseEntity<Response<Void>> createChatroom(
            ChatroomRequest chatroomRequest,
            AuthUser authUser
    ) {
        log.info("[POST] /v1/chatrooms : Controller 작동");

        Long userId = authUser.getUserId();

        chatroomCommandService.createChatroom(chatroomRequest, userId);

        return Response.success(null, ChatroomSuccessCode.CREATED_CHATROOM);
    }

    @Override
    @GetMapping
    public ResponseEntity<SliceResponse<ChatroomResponse>> getChatrooms(
            AuthUser authUser,
            int page,
            int size
    ) {
        log.info("[GET] /v1/chatrooms : Controller 작동");

        Long userId = authUser.getUserId();

        Pageable pageable = PageRequest.of(page, size);

        Slice<ChatroomResponse> chatroomResponses = chatroomQueryService.getChatrooms(userId, pageable);

        return SliceResponse.success(chatroomResponses, ChatroomSuccessCode.RETRIEVED_CHATROOMS);
    }

    @Override
    @DeleteMapping("/{chatroomId}")
    public ResponseEntity<Response<Void>> deleteChatroom(
            AuthUser authUser,
            Long chatroomId
    ) {
        log.info("[DELETE] /v1/chatrooms/{} : Controller 작동", chatroomId);

        Long userId = authUser.getUserId();

        chatroomCommandService.deleteChatroom(chatroomId, userId);

        return Response.success(null, ChatroomSuccessCode.DELETED_CHATROOM);
    }
}