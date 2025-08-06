package S13P11A708.backend.websocket;

import S13P11A708.backend.domain.enums.GameMessageType;

public class GameMessageFactory {

    public static GameSocketMessage createMessage(GameMessageType type, Long roomId, String payload) {
        return GameSocketMessage.builder()
                .type(type)
                .roomId(roomId)
                .payload(payload)
                .build();
    }

    public static GameSocketMessage createWithSender(GameMessageType type, Long roomId, Long senderId, String payload) {
        return GameSocketMessage.builder()
                .type(type)
                .roomId(roomId)
                .senderId(senderId)
                .payload(payload)
                .build();
    }

}
