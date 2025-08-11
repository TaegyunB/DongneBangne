package S13P11A708.backend;

import S13P11A708.backend.domain.enums.GameMessageType;
import S13P11A708.backend.domain.game.GameStatusRedis;
import S13P11A708.backend.dto.webSocket.GameHintSocketMessage;
import S13P11A708.backend.service.GameService;
import S13P11A708.backend.service.redis.GameRedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameHintSocketTest {
    @LocalServerPort
    int port;

    @Autowired
    GameService gameService;

    @MockitoBean
    GameRedisService gameRedisService; // Redis 로직 목킹

    WebSocketStompClient stomp;

    @BeforeEach
    void setup() {
        stomp = new WebSocketStompClient(new StandardWebSocketClient());
        stomp.setMessageConverter(new MappingJackson2MessageConverter());
    }

    // 1) 힌트 사용 불가 → HINT_REJECTED("힌트를 더 이상 사용할 수 없습니다.")
    @Test
    @Timeout(20)
    void hint_rejected_when_not_allowed() throws Exception {
        long roomId = 1L;
        long userId = 101L;

        // Redis 상태 더미
        GameStatusRedis state = GameStatusRedis.builder()
                .roomId(roomId)
                .round(1)
                .totalRound(2)
                .currentAnswer("고향역")
                .quizIdList(List.of(10L, 20L))
                .build();

        when(gameRedisService.getGameStatusRedis(roomId)).thenReturn(state);
        when(gameRedisService.canUseHint(roomId, userId)).thenReturn(false);

        String wsUrl = "ws://localhost:" + port + "/ws-game";
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch got1 = new CountDownLatch(1);
        CopyOnWriteArrayList<GameHintSocketMessage> inbox = new CopyOnWriteArrayList<>();

        StompSessionHandler handler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders ch) {
                connected.countDown();
                String dest = "/queue/hint/" + userId; // ← sendToUser(userId) 가 이 경로로 보낸다고 가정
                System.out.println("▶ [TEST] SUBSCRIBE " + dest);
                session.subscribe(dest, new StompFrameHandler() {
                    @Override public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
                        return GameHintSocketMessage.class;
                    }
                    @Override public void handleFrame(StompHeaders headers, Object payload) {
                        GameHintSocketMessage msg = (GameHintSocketMessage) payload;
                        System.out.println("📨 [TEST] RECV type=" + msg.getType() + ", payload='" + msg.getPayload() + "'");
                        inbox.add(msg);
                        got1.countDown();
                    }
                });

                System.out.println("▶ [TEST] TRIGGER handleHint(not allowed)");
                gameService.handleHint(roomId, userId);
            }
        };

        stomp.connectAsync(wsUrl, handler).get(5, TimeUnit.SECONDS);
        assertTrue(connected.await(5, TimeUnit.SECONDS));

        boolean ok = got1.await(10, TimeUnit.SECONDS);
        System.out.println("⏱️ [TEST] await=" + ok + ", inboxSize=" + inbox.size());
        inbox.forEach(m -> System.out.println("🔎 [TEST] inbox type=" + m.getType() + ", payload='" + m.getPayload() + "'"));
        assertTrue(ok);

        var types = inbox.stream().map(GameHintSocketMessage::getType).toList();
        assertTrue(types.contains(GameMessageType.HINT_REJECTED));
        assertEquals("힌트를 더 이상 사용할 수 없습니다.", inbox.get(0).getPayload());
    }

    // 2) 포인트 부족 → HINT_REJECTED("포인트가 부족합니다.")
    @Test
    @Timeout(20)
    void hint_rejected_when_not_enough_points() throws Exception {
        long roomId = 2L;
        long userId = 202L;

        GameStatusRedis state = GameStatusRedis.builder()
                .roomId(roomId).round(1).totalRound(2)
                .currentAnswer("고향역")
                .quizIdList(List.of(10L, 20L))
                .build();

        when(gameRedisService.getGameStatusRedis(roomId)).thenReturn(state);
        when(gameRedisService.canUseHint(roomId, userId)).thenReturn(true);
        when(gameRedisService.deductPointForHint(roomId, userId)).thenReturn(false); // 포인트 차감 실패

        String wsUrl = "ws://localhost:" + port + "/ws-game";
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch got1 = new CountDownLatch(1);
        CopyOnWriteArrayList<GameHintSocketMessage> inbox = new CopyOnWriteArrayList<>();

        StompSessionHandler handler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders ch) {
                connected.countDown();
                String dest = "/queue/hint/" + userId;
                System.out.println("▶ [TEST] SUBSCRIBE " + dest);
                session.subscribe(dest, new StompFrameHandler() {
                    @Override public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
                        return GameHintSocketMessage.class;
                    }
                    @Override public void handleFrame(StompHeaders headers, Object payload) {
                        GameHintSocketMessage msg = (GameHintSocketMessage) payload;
                        System.out.println("📨 [TEST] RECV type=" + msg.getType() + ", payload='" + msg.getPayload() + "'");
                        inbox.add(msg);
                        got1.countDown();
                    }
                });

                System.out.println("▶ [TEST] TRIGGER handleHint(not enough points)");
                gameService.handleHint(roomId, userId);
            }
        };

        stomp.connectAsync(wsUrl, handler).get(5, TimeUnit.SECONDS);
        assertTrue(connected.await(5, TimeUnit.SECONDS));

        boolean ok = got1.await(10, TimeUnit.SECONDS);
        System.out.println("⏱️ [TEST] await=" + ok + ", inboxSize=" + inbox.size());
        inbox.forEach(m -> System.out.println("🔎 [TEST] inbox type=" + m.getType() + ", payload='" + m.getPayload() + "'"));
        assertTrue(ok);

        var types = inbox.stream().map(GameHintSocketMessage::getType).toList();
        assertTrue(types.contains(GameMessageType.HINT_REJECTED));
        assertEquals("포인트가 부족합니다.", inbox.get(0).getPayload());
    }

    // 3) 성공 → HINT_RESPONSE(첫 글자)
    @Test
    @Timeout(20)
    void hint_success_sends_first_char() throws Exception {
        long roomId = 3L;
        long userId = 303L;
        String answer = "고향역";

        System.out.println("▶ [TEST-START] hint_success, roomId=" + roomId + ", userId=" + userId);

        GameStatusRedis state = GameStatusRedis.builder()
                .roomId(roomId).round(1).totalRound(2)
                .currentAnswer(answer)
                .quizIdList(List.of(10L, 20L))
                .build();

        when(gameRedisService.getGameStatusRedis(roomId)).thenReturn(state);
        when(gameRedisService.canUseHint(roomId, userId)).thenReturn(true);
        when(gameRedisService.deductPointForHint(roomId, userId)).thenReturn(true);
        doAnswer(inv -> {
            System.out.println("   [MOCK] markHintUsed called");
            return null;
        }).when(gameRedisService).markHintUsed(roomId, userId);

        System.out.println("✔ [SETUP] state.round=" + state.getRound()
                + ", total=" + state.getTotalRound()
                + ", currentAnswer='" + state.getCurrentAnswer() + "'");

        String wsUrl = "ws://localhost:" + port + "/ws-game";
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch got1 = new CountDownLatch(1);
        CopyOnWriteArrayList<GameHintSocketMessage> inbox = new CopyOnWriteArrayList<>();

        StompSessionHandler handler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders ch) {
                System.out.println("✅ [TEST] STOMP connected, sessionId=" + session.getSessionId());
                connected.countDown();

                String dest = "/queue/hint/" + userId;
                System.out.println("▶ [TEST] SUBSCRIBE " + dest);
                session.subscribe(dest, new StompFrameHandler() {
                    @Override public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
                        return GameHintSocketMessage.class;
                    }
                    @Override public void handleFrame(StompHeaders headers, Object payload) {
                        GameHintSocketMessage msg = (GameHintSocketMessage) payload;
                        System.out.println("📨 [TEST] RECV type=" + msg.getType()
                                + ", roomId=" + msg.getRoomId()
                                + ", targetUserId=" + msg.getUserId()
                                + ", payload='" + msg.getPayload() + "'");
                        inbox.add(msg);
                        got1.countDown();
                    }
                });

                System.out.println("▶ [TEST] TRIGGER handleHint(roomId=" + roomId + ", userId=" + userId + ")");
                gameService.handleHint(roomId, userId);
            }
        };

        StompSession sess = stomp.connectAsync(wsUrl, handler).get(5, TimeUnit.SECONDS);
        boolean connectedOk = connected.await(5, TimeUnit.SECONDS);
        System.out.println("⏱️ [TEST] connectedLatch=" + connectedOk);
        assertTrue(connectedOk);

        boolean awaited = got1.await(15, TimeUnit.SECONDS);
        System.out.println("⏱️ [TEST] await=" + awaited + ", inboxSize=" + inbox.size());
        inbox.forEach(m -> System.out.println("🔎 [TEST] inbox item: type=" + m.getType()
                + ", payload='" + m.getPayload() + "'"));
        assertTrue(awaited);

        // --- 검증/추가 출력 ---
        GameHintSocketMessage msg = inbox.get(0);
        System.out.println("✔ [ASSERT] expecting HINT_RESPONSE & first char='" + answer.substring(0,1) + "'");
        assertEquals(GameMessageType.HINT_RESPONSE, msg.getType());
        assertEquals(roomId, msg.getRoomId());
        assertEquals(answer.substring(0,1), msg.getPayload());

        sess.disconnect();
        System.out.println("🧹 [TEST] session disconnected");

    }

}
