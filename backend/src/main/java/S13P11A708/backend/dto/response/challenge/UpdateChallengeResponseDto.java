package S13P11A708.backend.dto.response.challenge;

import S13P11A708.backend.domain.Challenge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChallengeResponseDto {

    private Long id;
    private String challengeTitle;
    private String challengePlace;
    private String description;

    public static UpdateChallengeResponseDto from(Challenge challenge) {
        return UpdateChallengeResponseDto.builder()
                .id(challenge.getId())
                .challengeTitle(challenge.getChallengeTitle())
                .challengePlace(challenge.getChallengePlace())
                .description(challenge.getDescription())
                .build();
    }
}
