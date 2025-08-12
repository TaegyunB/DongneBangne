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
public class GameHintRequestMessage {
    private GameMessageType type;
    private Long roomId;
    private Long userId;
}
