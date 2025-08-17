package S13P11A708.backend.websocket;

import S13P11A708.backend.domain.enums.GameMessageType;
import S13P11A708.backend.dto.webSocket.GameAnsSocketMessage;
import S13P11A708.backend.dto.webSocket.GameEndSocketMessage;
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

    public static GameAnsSocketMessage createAnsMessage(GameMessageType type, Long roomId, Long userId, boolean isCorrect) {
        return GameAnsSocketMessage.builder()
                .type(type)
                .roomId(roomId)
                .userId(userId)
                .isCorrect(isCorrect)
                .build();
    }

    public static GameHintSocketMessage createHintMessage(GameMessageType type, Long roomId, Long userId, boolean canUse, String payload) {
        return GameHintSocketMessage.builder()
                .type(type)
                .roomId(roomId)
                .userId(userId)
                .canUse(canUse)
                .payload(payload)
                .build();
    }

    public static GameEndSocketMessage createEndMessage(GameMessageType type, Long roomId, Long winnerId, String winnerNickname, Long userPoint1, Long userPoint2, String payload){
        return GameEndSocketMessage.builder()
                .type(type)
                .roomId(roomId)
                .winnerId(winnerId)
                .winnerNickname(winnerNickname)
                .userPoint1(userPoint1)
                .userPoint2(userPoint2)
                .payload(payload)
                .build();

    }

}
