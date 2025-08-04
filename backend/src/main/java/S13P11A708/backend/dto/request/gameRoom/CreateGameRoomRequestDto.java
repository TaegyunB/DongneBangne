package S13P11A708.backend.dto.request.gameRoom;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateGameRoomRequestDto {
    String roomTitle;
    Integer gameRound;
    String musicEra;
    String category;
}
