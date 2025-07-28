package S13P11A708.backend.security;

import S13P11A708.backend.domain.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserDto {
    private Long userId;
    private String userName;
    private String nickname;
    private String profileImage;
    private UserRole userRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
