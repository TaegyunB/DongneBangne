package S13P11A708.backend.dto.webSocket;

import S13P11A708.backend.domain.enums.GameMessageType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameAnsSocketMessage {
    private GameMessageType type;
    private Long roomId;
    private Long userId;
    private String answer;
    private boolean isCorrect;
    private String payload;
}
