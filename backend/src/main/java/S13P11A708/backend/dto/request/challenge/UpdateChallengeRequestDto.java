package S13P11A708.backend.dto.request.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChallengeRequestDto {

    private String challengeId;

    private String challengeTitle;

    private String challengePlace;

    private String description;


}
