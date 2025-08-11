package S13P11A708.backend.dto.response.gameRoomUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GameRoomUserResponseDto {
    private Long gameRoomId; //어떤 게임방인지
    private Long userId; //게임방에 속한 유저
    private Boolean ready; //준비 상태
    private String message; //action 메세지
}
