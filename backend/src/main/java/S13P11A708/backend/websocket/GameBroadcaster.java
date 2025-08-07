package S13P11A708.backend.websocket;

import S13P11A708.backend.dto.webSocket.GameAnsSocketMessage;
import S13P11A708.backend.dto.webSocket.GameHintSocketMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    // 전체 방에 브로드캐스트
    public void broadcastToRoom(Long roomId, GameAnsSocketMessage message){
        messagingTemplate.convertAndSend("/sub/game/" + roomId, message);
    }

    // 특정 유저에게만 전송 (힌트)
    public void sendToUser(Long userId, GameHintSocketMessage message) {
        messagingTemplate.convertAndSend("/queue/hint/"+userId, message);
    }
}
