package S13P11A708.backend.dto.response.seniorCenter;

import S13P11A708.backend.domain.SeniorCenter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public static SeniorCenterRankingDto from(SeniorCenter seniorCenter, Integer ranking) {
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
                .build();
    }

}
