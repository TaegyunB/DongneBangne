package S13P11A708.backend.dto.request;

import S13P11A708.backend.domain.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserRequestDto {
    //카카오 OAuth 정보처리
    private Long userId;
    private String userName;
    private String nickname;
    private String profileImage;
    private UserRole userRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
