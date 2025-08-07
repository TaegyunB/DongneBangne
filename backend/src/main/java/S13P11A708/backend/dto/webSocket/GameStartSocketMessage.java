package S13P11A708.backend.dto.webSocket;

import S13P11A708.backend.domain.enums.GameMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameStartSocketMessage {
    private GameMessageType type;
    private Long roomId;
    private String payload;
    private Long user1Id;
    private Long user2Id;
}
