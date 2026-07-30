package com.apu.roommate_filter.service;

import com.apu.roommate_filter.model.AppUser;
import com.apu.roommate_filter.model.ChatMessage;
import com.apu.roommate_filter.model.Match;
import com.apu.roommate_filter.repository.AppUserRepository;
import com.apu.roommate_filter.repository.ChatMessageRepository;
import com.apu.roommate_filter.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private ChatMessageRepository chatMessageRepository;
    private MatchRepository matchRepository;
    private AppUserRepository appUserRepository;
    private ChatService chatService;

    private AppUser alice;
    private AppUser bob;
    private AppUser outsider;
    private Match match;

    @BeforeEach
    void setUp() {
        chatMessageRepository = mock(ChatMessageRepository.class);
        matchRepository       = mock(MatchRepository.class);
        appUserRepository     = mock(AppUserRepository.class);
        chatService = new ChatService(chatMessageRepository, matchRepository, appUserRepository);

        alice    = user(1L, "Alice");
        bob      = user(2L, "Bob");
        outsider = user(3L, "Rohan");

        match = new Match();
        match.setId(1L);
        match.setUser1(alice);
        match.setUser2(bob);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(appUserRepository.findById(3L)).thenReturn(Optional.of(outsider));
        when(chatMessageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private AppUser user(Long id, String firstName) {
        AppUser u = new AppUser();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName("Test");
        return u;
    }

    @Test
    void non_participant_cannot_post_into_a_match() {
        // Regression for BE-02: the WebSocket path persisted messages from anyone, so a third
        // party could inject into a private conversation. Only getHistory was guarded.
        assertThatThrownBy(() ->
                chatService.saveMessage(1L, outsider.getId(), "QA websocket test", "TEXT"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not a participant");

        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void participant_can_post_into_their_match() {
        var dto = chatService.saveMessage(1L, alice.getId(), "Hello", "TEXT");

        assertThat(dto.getSenderId()).isEqualTo(alice.getId());
        assertThat(dto.getContent()).isEqualTo("Hello");
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }
}
