package S13P11A708.backend.dto.response.challenge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyChallengeResponseDto {

    private Integer year;
    private Integer month;
    private List<ChallengeResponseDto> serviceChallenges;
    private List<ChallengeResponseDto> customChallenges;
    private Integer totalCount;

}
