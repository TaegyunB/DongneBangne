package S13P11A708.backend;


import S13P11A708.backend.dto.webSocket.GameAnsSocketMessage;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SocketTest {

    private static final String WS_URL = "ws://localhost:8080/ws-game";

    @Test
    void connect_subscribe_send_should_work() throws Exception {
        WebSocketStompClient stomp = new WebSocketStompClient(new StandardWebSocketClient());
        stomp.setMessageConverter(new MappingJackson2MessageConverter());

        StompHeaders connectHeaders = new StompHeaders();
        // JWT 쓰면 여기에 헤더 추가
         connectHeaders.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInJvbGUiOiJST0xFX0dVRVNUIiwiaWF0IjoxNzU0ODE0OTQ2LCJleHAiOjE3NTQ5MDEzNDZ9.FFi9BaxsRDXfl4m7MuwKmTzb_JDBmnrSzfx9flkjFuE");

        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch messageArrived = new CountDownLatch(1);

        StompSessionHandler handler = new StompSessionHandlerAdapter() {
            @Override public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                connected.countDown();
                // 구독 (서버에서 이 경로로 방송한다고 가정: /sub/games/1)
                session.subscribe("/sub/games/1", new StompFrameHandler() {
                    @Override public java.lang.reflect.Type getPayloadType(StompHeaders headers) { return GameAnsSocketMessage.class; }
                    @Override public void handleFrame(StompHeaders headers, Object payload) {
                        GameAnsSocketMessage msg = (GameAnsSocketMessage) payload;
                        System.out.println("📩 type=" + msg.getType() +
                                ", roomId=" + msg.getRoomId() +
                                ", isCorrect=" + msg.isCorrect());
                        messageArrived.countDown();
                    }
                });

                // 메시지 전송 (@MessageMapping("/games/answer") → /pub/games/answer)
                session.send("/pub/games/answer", new AnsPayload(1L, "테스트정답"));
            }

            @Override public void handleException(StompSession s, StompCommand c, StompHeaders h, byte[] p, Throwable ex) {
                ex.printStackTrace();
            }
            @Override public void handleTransportError(StompSession s, Throwable ex) {
                ex.printStackTrace();
            }
        };

        CompletableFuture<StompSession> future =
                stomp.connectAsync(WS_URL, new WebSocketHttpHeaders(), connectHeaders, handler);

        StompSession session = future.get(5, TimeUnit.SECONDS);
        assertTrue(connected.await(5, TimeUnit.SECONDS));

        // 서버가 /sub/games/1 로 응답을 쏘는 시점에 맞춰 5~10초 대기
        messageArrived.await(10, TimeUnit.SECONDS);

        session.disconnect();
    }

    // 전송 DTO (GameAnsSocketMessage에 맞게 필드명 조정)
    static class AnsPayload {
        public Long roomId;
        public String payload;
        public AnsPayload(Long roomId, String payload) { this.roomId = roomId; this.payload = payload; }
    }
}
