package S13P11A708.backend.dto.response.gameRoomUser;

import S13P11A708.backend.domain.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReadyGameRoomUserResponseDto {
    private Long gameRoomId; //어떤 게임방인지
    private Long userId_1; //게임방에 속한 유저1
    private Long userId_2; //게임방에 속한 유저2
    private GameStatus gameStatus; //게임 상태
    private String message; //준비 메세지
}
