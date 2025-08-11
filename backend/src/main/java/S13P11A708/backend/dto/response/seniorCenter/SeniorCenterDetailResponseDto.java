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
public class SeniorCenterDetailResponseDto {

    private Long seniorCenterId;
    private Long adminUserId;
    private String centerName;
    private String address;
    private Long trotPoint;
    private Long challengePoint;
    private Long totalPoint;
    private Integer rankingYear;
    private Integer rankingMonth;

    public static SeniorCenterDetailResponseDto from(SeniorCenter seniorCenter) {
        return SeniorCenterDetailResponseDto.builder()
                .seniorCenterId(seniorCenter.getId())
                .adminUserId(seniorCenter.getAdminUserId())
                .centerName(seniorCenter.getCenterName())
                .address(seniorCenter.getAddress())
                .trotPoint(seniorCenter.getTrotPoint())
                .challengePoint(seniorCenter.getChallengePoint())
                .totalPoint(seniorCenter.getTotalPoint())
                .rankingYear(seniorCenter.getRankingYear())
                .rankingMonth(seniorCenter.getRankingMonth())
                .build();
    }
}
