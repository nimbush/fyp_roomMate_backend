package com.apu.roommate_filter.controller;

import com.apu.roommate_filter.dto.util.ChatMessageDto;
import com.apu.roommate_filter.dto.util.ConversationDto;
import com.apu.roommate_filter.service.ChatService;
import com.apu.roommate_filter.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{matchId}/messages")
    public ResponseEntity<?> getHistory(@PathVariable Long matchId) {
        try {
            Long callerId = SecurityUtils.getCallerUserId();
            List<ChatMessageDto> messages = chatService.getHistory(matchId, callerId);
            return ResponseEntity.ok(messages);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(@RequestParam Long userId) {
        if (!SecurityUtils.getCallerUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        try {
            List<ConversationDto> conversations = chatService.getConversations(userId);
            return ResponseEntity.ok(conversations);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
