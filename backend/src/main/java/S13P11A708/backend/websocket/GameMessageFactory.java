package S13P11A708.backend.websocket;

import S13P11A708.backend.domain.enums.GameMessageType;
import S13P11A708.backend.dto.webSocket.GameAnsSocketMessage;
import S13P11A708.backend.dto.webSocket.GameHintSocketMessage;
import S13P11A708.backend.dto.webSocket.GameInfoSocketMessage;
import org.springframework.stereotype.Component;

@Component
public class GameMessageFactory {

    public static GameInfoSocketMessage createInfoMessage(GameMessageType type, Long roomId, String payload) {
        return GameInfoSocketMessage.builder()
                .type(type)
                .roomId(roomId)
                .payload(payload)
                .build();
    }

    public static GameAnsSocketMessage createAnsMessage(GameMessageType type, Long roomId, String payload) {
        return GameAnsSocketMessage.builder()
                .type(type)
                .roomId(roomId)
                .payload(payload)
                .build();
    }

    public static GameHintSocketMessage createHintMessage(GameMessageType type, Long roomId, Long userId, String payload) {
        return GameHintSocketMessage.builder()
                .type(type)
                .roomId(roomId)
                .userId(userId)
                .payload(payload)
                .build();
    }

}
