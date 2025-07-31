package S13P11A708.backend.dto.response.challenge;

import S13P11A708.backend.domain.Challenge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateChallengeResponseDto {

    private Long id;
    private String challengeTitle;
    private String challengePlace;
    private String description;
    private int year;
    private int month;
    private Integer point;
    private Boolean isSuccess;
    private Long seniorCenterId;
    private String seniorCenterName;

    /**
     * Entitiy를 DTO로 변환하는 정적 메서드
     */
    public static CreateChallengeResponseDto from(Challenge challenge) {
        return CreateChallengeResponseDto.builder()
                .id(challenge.getId())
                .challengeTitle(challenge.getChallengeTitle())
                .challengePlace(challenge.getChallengePlace())
                .description(challenge.getDescription())
                .year(challenge.getYear())
                .month(challenge.getMonth())
                .point(challenge.getPoint())
                .isSuccess(challenge.getIsSuccess())
                .seniorCenterId(challenge.getSeniorCenter() != null ? challenge.getSeniorCenter().getId() : null)
                .seniorCenterName(challenge.getSeniorCenter() != null ? challenge.getSeniorCenter().getCenterName() : null)
                .build();
    }

}
