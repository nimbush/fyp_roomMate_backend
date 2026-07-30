package com.apu.roommate_filter.controller;

import com.apu.roommate_filter.dto.util.ChatMessageDto;
import com.apu.roommate_filter.dto.util.RealtimeChatMessage;
import com.apu.roommate_filter.model.AppUserPrincipal;
import com.apu.roommate_filter.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.send/{matchId}")
    public void sendMessage(
            @DestinationVariable Long matchId,
            @Payload RealtimeChatMessage message,
            Principal principal
    ) {
        AppUserPrincipal appUserPrincipal =
                (AppUserPrincipal) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Long senderId = appUserPrincipal.getAppUser().getId();

        ChatMessageDto saved = chatService.saveMessage(
                matchId, senderId, message.getContent(), message.getMessageType());

        messagingTemplate.convertAndSend("/topic/chat/" + matchId, saved);
    }
}
