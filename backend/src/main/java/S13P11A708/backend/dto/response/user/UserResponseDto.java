package S13P11A708.backend.dto.response.user;

import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.domain.enums.UserRole;
import S13P11A708.backend.dto.response.seniorCenter.SeniorCenterDetailResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long userId;
    private String nickname;
    private String profileImage;
    private UserRole userRole;
    private Long personalPoint;
    private SeniorCenterDetailResponseDto seniorCenter;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .userRole(user.getUserRole())
                .personalPoint(user.getPersonalPoint())
                .seniorCenter(SeniorCenterDetailResponseDto.from(user.getSeniorCenter()))
                .build();
    }


}
