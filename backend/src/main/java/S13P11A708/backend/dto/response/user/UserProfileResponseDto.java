package S13P11A708.backend.dto.response.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponseDto {
    private String nickname;
    private String profileImage;
}
