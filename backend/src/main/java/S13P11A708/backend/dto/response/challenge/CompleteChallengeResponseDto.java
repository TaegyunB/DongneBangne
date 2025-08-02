package S13P11A708.backend.dto.response.challenge;

import S13P11A708.backend.domain.Challenge;
import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.dto.response.seniorCenter.SeniorCenterPointResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteChallengeResponseDto {

    private Long challengeId;
    private String challengeTitle;
    private Boolean isSuccess;
    private Integer earnedPoint;
    private SeniorCenterPointResponseDto seniorCenter;

    public static CompleteChallengeResponseDto from(Challenge challenge, SeniorCenter updatedSeniorCenter) {
        return CompleteChallengeResponseDto.builder()
                .challengeId(challenge.getId())
                .challengeTitle(challenge.getChallengeTitle())
                .isSuccess(challenge.getIsSuccess())
                .earnedPoint(challenge.getPoint())
                .seniorCenter(SeniorCenterPointResponseDto.from(updatedSeniorCenter))
                .build();
    }
}
