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
public class ChallengeResponseDto {

    private Long id;
    private String challengeTitle;
    private String challengePlace;
    private String description;
    private Integer year;
    private Integer month;
    private Integer point;
    private String challengeImage;
    private String imageDescription;
    private String aiDescription;
    private Boolean isSuccess;
    private String seniorCenterName;

    public static ChallengeResponseDto from(Challenge challenge) {
        return ChallengeResponseDto.builder()
                .id(challenge.getId())
                .challengeTitle(challenge.getChallengeTitle())
                .challengePlace(challenge.getChallengePlace())
                .description(challenge.getDescription())
                .year(challenge.getYear())
                .month(challenge.getMonth())
                .point(challenge.getPoint())
                .challengeImage(challenge.getChallengeImage())
                .imageDescription(challenge.getImageDescription())
                .aiDescription(challenge.getAiDescription())
                .isSuccess(challenge.getIsSuccess())
                .seniorCenterName(challenge.getSeniorCenter().getCenterName())
                .build();
    }
}
