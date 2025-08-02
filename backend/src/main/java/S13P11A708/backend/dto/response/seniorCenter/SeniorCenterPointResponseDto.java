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
public class SeniorCenterPointResponseDto {

    private Long seniorCenterId;
    private String seniorCenterName;
    private Long totalPoint;
    private Long challengePoint;
    private Long trotPoint;

    public static SeniorCenterPointResponseDto from(SeniorCenter seniorCenter) {
        return SeniorCenterPointResponseDto.builder()
                .seniorCenterId(seniorCenter.getId())
                .seniorCenterName(seniorCenter.getCenterName())
                .totalPoint(seniorCenter.getTotalPoint())
                .challengePoint(seniorCenter.getTotalPoint())
                .trotPoint(seniorCenter.getTrotPoint())
                .build();
    }
}
