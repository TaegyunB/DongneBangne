package S13P11A708.backend.websocket;

import S13P11A708.backend.security.CustomOAuth2User;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SignalingHandler extends TextWebSocketHandler {

    // roomId → Set of sessions
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(SignalingHandler.class);
    //private final Set<WebSocketSession> clients = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String uri = session.getUri().toString(); // ex: /signal/2
        String roomId = extractRoomId(uri);

        // 방이 없으면 새로 만들고, 있으면 기존 참가자 수 확인
        rooms.computeIfAbsent(roomId, k -> Collections.synchronizedSet(new HashSet<>()));

        if (rooms.get(roomId).size() >= 2) {
            log.warn("❗ room {} 은 이미 정원이 찼습니다. 연결을 거절합니다.", roomId);
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Room full (2 users max)"));
            return;
        }

        rooms.get(roomId).add(session);
        log.info("📡 연결됨: {} → room {}", session.getId(), roomId);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // roomId 추출
        String uri = session.getUri().toString();
        String roomId = extractRoomId(uri);

        log.info("📨 {} → {}: {}", session.getId(), roomId, message.getPayload());

        for (WebSocketSession client : rooms.getOrDefault(roomId, Collections.emptySet())) {
            if (!client.equals(session) && client.isOpen()) {
                client.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        String uri = session.getUri().toString();
        String roomId = extractRoomId(uri);

        Set<WebSocketSession> room = rooms.get(roomId);
        if (room != null) {
            room.remove(session);
            log.info("❌ 연결 종료: {} → room {}", session.getId(), roomId);

            if (room.isEmpty()) {
                rooms.remove(roomId);
                log.info("🧹 빈 방 정리: room {}", roomId);
            }
        }


//        clients.remove(session);
//        log.info("❌ 클라이언트 연결 종료: {}", session.getId());
    }

    // roomId 추출 함수
    private String extractRoomId(String uri) {
        return uri.substring(uri.lastIndexOf("/") + 1);
    }
}