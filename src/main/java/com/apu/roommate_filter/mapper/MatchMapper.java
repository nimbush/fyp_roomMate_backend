package com.apu.roommate_filter.mapper;

import com.apu.roommate_filter.dto.util.MatchDto;
import com.apu.roommate_filter.model.AppUser;
import com.apu.roommate_filter.model.Match;

public class MatchMapper {

    public static MatchDto toDTO(Match match) {
        return toDTO(match, null);
    }

    /**
     * @param callerId the user the response is being built for; when supplied, the other
     *                 participant's id and display name are filled in so the client can render a
     *                 name instead of "User #3".
     */
    public static MatchDto toDTO(Match match, Long callerId) {
        MatchDto dto = new MatchDto();
        dto.setId(match.getId());
        dto.setUser1Id(match.getUser1().getId());
        dto.setUser2Id(match.getUser2().getId());
        dto.setMatchScore(match.getMatchScore());
        dto.setStatus(match.getStatus().name());
        dto.setMatchedAt(match.getMatchedAt());
        dto.setLastInteraction(match.getLastInteraction());

        if (callerId != null) {
            AppUser other = match.getUser1().getId().equals(callerId)
                    ? match.getUser2()
                    : match.getUser1();
            dto.setOtherUserId(other.getId());
            dto.setOtherUserName(other.getFullName());
        }
        return dto;
    }
}
