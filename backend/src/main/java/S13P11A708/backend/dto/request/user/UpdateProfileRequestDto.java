package S13P11A708.backend.dto.request.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequestDto {
    private String nickname;
    private String profileImage;
}
