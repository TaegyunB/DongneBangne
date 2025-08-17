package S13P11A708.backend.dto.webSocket;

import S13P11A708.backend.domain.enums.GameMessageType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameAnsRequestMessage {
    private String answer;
    private Long roomId;
    private GameMessageType type;
    private Long userId;
}
