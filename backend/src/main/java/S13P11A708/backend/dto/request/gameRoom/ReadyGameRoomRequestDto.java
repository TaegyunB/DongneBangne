package S13P11A708.backend.dto.request.gameRoom;

import S13P11A708.backend.domain.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadyGameRoomRequestDto {
    private Long gameRoomId;
    private Long userId;
    private GameStatus gameStatus;
    private Boolean ready;
}
