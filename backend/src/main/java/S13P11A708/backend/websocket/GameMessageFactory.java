package S13P11A708.backend.websocket;

import S13P11A708.backend.domain.enums.GameMessageType;
import S13P11A708.backend.dto.webSocket.GameAnsSocketMessage;
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

    public static GameAnsSocketMessage createWithSender(GameMessageType type, Long roomId, Long senderId, String payload) {
        return GameAnsSocketMessage.builder()
                .type(type)
                .roomId(roomId)
                .senderId(senderId)
                .payload(payload)
                .build();
    }

}
