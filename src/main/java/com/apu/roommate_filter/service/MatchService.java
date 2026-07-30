package com.apu.roommate_filter.service;

import com.apu.roommate_filter.dto.util.MatchDto;
import com.apu.roommate_filter.mapper.MatchMapper;
import com.apu.roommate_filter.model.Match;
import com.apu.roommate_filter.repository.MatchRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final ImageService imageService;

    public List<MatchDto> getAllMatchesForUser(Long userId) {
        return matchRepository.findAllByUser1IdOrUser2Id(userId, userId)
                .stream().map(m -> toDtoForCaller(m, userId)).collect(Collectors.toList());
    }

    public List<MatchDto> getAcceptedMatches(Long userId) {
        return matchRepository.findAllAcceptedMatches(userId)
                .stream().map(m -> toDtoForCaller(m, userId)).collect(Collectors.toList());
    }

    public List<MatchDto> getPendingMatchesForUser(Long userId) {
        return matchRepository.findPendingMatchesWhereUserIsUser2(userId)
                .stream().map(m -> toDtoForCaller(m, userId)).collect(Collectors.toList());
    }

    /** Adds the other participant's name (via the mapper) and first photo, for list rendering. */
    private MatchDto toDtoForCaller(Match match, Long callerId) {
        MatchDto dto = MatchMapper.toDTO(match, callerId);
        if (dto.getOtherUserId() != null) {
            imageService.getImagesForUser(dto.getOtherUserId()).stream()
                    .findFirst()
                    .ifPresent(image -> dto.setOtherUserImage(image.getImageData()));
        }
        return dto;
    }

    @Transactional
    public MatchDto acceptMatch(Long matchId, Long callerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        requireParticipant(match, callerId);

        if (match.getStatus() != Match.MatchStatus.PENDING) {
            throw new RuntimeException("Match is not in pending status");
        }

        match.setStatus(Match.MatchStatus.ACCEPTED);
        match.setLastInteraction(LocalDateTime.now());
        return toDtoForCaller(matchRepository.save(match), callerId);
    }

    @Transactional
    public MatchDto rejectMatch(Long matchId, Long callerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        requireParticipant(match, callerId);

        match.setStatus(Match.MatchStatus.REJECTED);
        match.setLastInteraction(LocalDateTime.now());
        return toDtoForCaller(matchRepository.save(match), callerId);
    }

    private void requireParticipant(Match match, Long callerId) {
        boolean isParticipant = match.getUser1().getId().equals(callerId)
                || match.getUser2().getId().equals(callerId);
        if (!isParticipant) {
            throw new AccessDeniedException("You are not a participant in this match");
        }
    }
}
