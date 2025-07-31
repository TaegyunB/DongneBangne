package S13P11A708.backend.dto.request.challenge;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateChallengeCreateRequestDto {

    @NotBlank(message = "도전 제목은 필수입니다.")
    private String challengeTitle;

    private String challengePlace;

    @NotBlank(message = "도전 설명은 필수입니다.")
    private String description;

}
