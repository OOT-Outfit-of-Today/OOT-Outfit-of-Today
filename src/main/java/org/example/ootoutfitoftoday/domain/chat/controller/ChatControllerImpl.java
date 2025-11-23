package org.example.ootoutfitoftoday.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.response.SliceResponse;
import org.example.ootoutfitoftoday.domain.chat.dto.response.ChatResponse;
import org.example.ootoutfitoftoday.domain.chat.exception.ChatSuccessCode;
import org.example.ootoutfitoftoday.domain.chat.service.query.ChatQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/chatrooms/{chatroomId}/chats")
@RequiredArgsConstructor
public class ChatControllerImpl implements ChatController {

    private final ChatQueryService chatQueryService;

    @Override
    @GetMapping
    public ResponseEntity<SliceResponse<ChatResponse>> getChats(
            Long chatroomId,
            int page,
            int size
    ) {

        log.info("[GET] /v1/chatrooms/{}/chats : Controller 작동", chatroomId);

        Pageable pageable = PageRequest.of(page, size);

        Slice<ChatResponse> chatResponses = chatQueryService.getChats(chatroomId, pageable);

        return SliceResponse.success(chatResponses, ChatSuccessCode.RETRIEVED_CHATS);
    }
}
