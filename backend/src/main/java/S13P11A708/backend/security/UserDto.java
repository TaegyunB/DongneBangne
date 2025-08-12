package S13P11A708.backend.security;

import S13P11A708.backend.domain.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long userId; //동네방네 유저 식별 id
    private String kakaoId; // 카카오 id
    private String nickname; // 사용자 이름
    private String profileImage;
    private UserRole userRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
