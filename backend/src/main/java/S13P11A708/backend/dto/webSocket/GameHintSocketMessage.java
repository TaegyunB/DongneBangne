package S13P11A708.backend.dto.webSocket;

import S13P11A708.backend.domain.enums.GameMessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameHintSocketMessage {
    private GameMessageType type;
    private Long roomId;
}
