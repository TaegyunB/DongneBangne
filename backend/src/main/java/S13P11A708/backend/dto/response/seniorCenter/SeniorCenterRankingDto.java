package S13P11A708.backend.dto.response.seniorCenter;

import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.dto.response.challenge.ChallengeStatusResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeniorCenterRankingDto {

    private Long seniorCenterId;
    private String seniorCenterName;
    private String address;
    private Long totalPoint;
    private Long challengePoint;
    private Long trotPoint;
    private Integer ranking;
    private Integer rankingYear;
    private Integer rankingMonth;
    private List<ChallengeStatusResponseDto> challenges;

    public static SeniorCenterRankingDto from(SeniorCenter seniorCenter, Integer ranking) {

        List<ChallengeStatusResponseDto> challengeStatusResponseDto = Collections.emptyList();

        if (seniorCenter.getChallenges() != null) {
            challengeStatusResponseDto = seniorCenter.getChallenges().stream()
                    .map(ChallengeStatusResponseDto::from)
                    .collect(Collectors.toList());
        }

        return SeniorCenterRankingDto.builder()
                .seniorCenterId(seniorCenter.getId())
                .seniorCenterName(seniorCenter.getCenterName())
                .address(seniorCenter.getAddress())
                .totalPoint(seniorCenter.getTotalPoint())
                .challengePoint(seniorCenter.getTotalPoint())
                .trotPoint(seniorCenter.getTrotPoint())
                .ranking(ranking)
                .rankingYear(seniorCenter.getRankingYear())
                .rankingMonth(seniorCenter.getRankingMonth())
                .challenges(challengeStatusResponseDto)
                .build();
    }

}
