package S13P11A708.backend.dto.request.aiNews;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAiNewsRequestDto {

    private Long seniorCenterId;
    private Integer year;
    private Integer month;
}
