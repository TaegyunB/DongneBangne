package S13P11A708.backend.dto.request.gameRoom;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JoinGameRoomRequestDto {
    private Long roomId; //참여하는 게임방 id
    private Long userId;
}
