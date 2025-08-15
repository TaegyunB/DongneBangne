package S13P11A708.backend.websocket;

import S13P11A708.backend.security.CustomOAuth2User;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SignalingHandler extends TextWebSocketHandler {

    //private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(SignalingHandler.class);
    private final Set<WebSocketSession> clients = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        clients.add(session);
        log.info("📡 클라이언트 연결됨: {}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("📨 수신: {}", message.getPayload());

        for (WebSocketSession client : clients) {
            if (!client.equals(session) && client.isOpen()) {
                client.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        clients.remove(session);
        log.info("❌ 클라이언트 연결 종료: {}", session.getId());
    }
}