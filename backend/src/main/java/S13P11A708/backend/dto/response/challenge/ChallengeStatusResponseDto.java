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
public class ChallengeStatusResponseDto {

    private Long challengeId;
    private String challengeTitle;
    private Boolean isSuccess;

    public static ChallengeStatusResponseDto from(Challenge challenge) {
        return ChallengeStatusResponseDto.builder()
                .challengeId(challenge.getId())
                .challengeTitle(challenge.getChallengeTitle())
                .isSuccess(challenge.getIsSuccess())
                .build();
    }
}
