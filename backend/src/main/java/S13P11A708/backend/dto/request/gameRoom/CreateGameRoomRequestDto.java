package S13P11A708.backend.dto.request.gameRoom;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateGameRoomRequestDto {
    private String roomTitle;
    private Integer gameRound;
    private String musicEra;
    private String category;
}
