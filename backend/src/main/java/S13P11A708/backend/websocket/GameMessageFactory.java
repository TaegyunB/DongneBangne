package S13P11A708.backend.websocket;

import S13P11A708.backend.domain.enums.GameMessageType;
import S13P11A708.backend.dto.webSocket.GameAnsSocketMessage;
import S13P11A708.backend.dto.webSocket.GameHintSocketMessage;
import org.springframework.stereotype.Component;

@Component
public class GameMessageFactory {

    public static GameAnsSocketMessage createMessage(GameMessageType type, Long roomId, String payload) {
        return GameAnsSocketMessage.builder()
                .type(type)
                .roomId(roomId)
                .payload(payload)
                .build();
    }

    public static GameHintSocketMessage createWithSender(GameMessageType type, Long roomId, String payload) {
        return GameHintSocketMessage.builder()
                .type(type)
                .roomId(roomId)
                .payload(payload)
                .build();
    }

}
