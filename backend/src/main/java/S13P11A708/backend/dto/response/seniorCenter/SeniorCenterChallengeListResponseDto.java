package S13P11A708.backend.dto.response.seniorCenter;

import S13P11A708.backend.dto.response.challenge.ChallengeResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeniorCenterChallengeListResponseDto {

    private Long seniorCenterId;
    private String seniorCenterName;
    private String address;
    private List<ChallengeResponseDto> challenges;

}
