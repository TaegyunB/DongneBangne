package S13P11A708.backend.dto.request.challenge;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChallengeRequestDto {

    private String challengeTitle;

    private String challengePlace;

    private String description;


}
