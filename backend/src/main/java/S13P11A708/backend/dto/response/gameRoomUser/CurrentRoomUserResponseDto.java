package S13P11A708.backend.dto.response.gameRoomUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CurrentRoomUserResponseDto {
    private Long gameRoomId; //어떤 게임방인지
    private Long userId; //게임방에 속한 유저
    private String nickname; //유저 이름
    private String seniorCenter; //유저가 속한 경로당
    private Boolean ready; //준비 상태
}
