package S13P11A708.backend.dto.webSocket;

import S13P11A708.backend.domain.enums.GameMessageType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameEndSocketMessage {
    private GameMessageType type;
    private Long roomId;
    private Long winnerId;
    private String winnerNickname;
    private Long userPoint1; //user1의 personalPoint
    private Long userPoint2;
    private String payload;

}
