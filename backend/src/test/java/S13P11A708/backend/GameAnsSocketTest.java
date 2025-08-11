package S13P11A708.backend;

import S13P11A708.backend.domain.TrotQuiz;
import S13P11A708.backend.domain.enums.GameMessageType;
import S13P11A708.backend.domain.game.GameStatusRedis;
import S13P11A708.backend.domain.game.PlayerStatus;
import S13P11A708.backend.dto.webSocket.GameInfoSocketMessage;
import S13P11A708.backend.repository.TrotQuizRepository;
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
public class GameAnsSocketTest {
    @LocalServerPort
    int port;

    @Autowired
    GameService gameService;

    @MockitoBean
    GameRedisService gameRedisService;     // Redis 의존성 목킹
    @MockitoBean
    TrotQuizRepository trotQuizRepository; // 다음 라운드 문제 조회 목킹

    WebSocketStompClient stomp;

    @BeforeEach
    void init() {
        stomp = new WebSocketStompClient(new StandardWebSocketClient());
        stomp.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    @Timeout(20)
    void answer_correct_then_next_question_broadcasted() throws Exception {
        long roomId = 1L;
        long user1 = 101L;
        String correct = "고향역";

        System.out.println("▶ [TEST-START] roomId=" + roomId + ", user1=" + user1 + ", correct='" + correct + "'");

        // --- Redis 상태 더미 구성 ---
        GameStatusRedis state = GameStatusRedis.builder()
                .roomId(roomId)
                .round(1)
                .totalRound(2)
                .currentAnswer(correct)
                .quizIdList(List.of(10L, 20L))
                .build();

        when(gameRedisService.getGameStatusRedis(roomId)).thenReturn(state);
        when(gameRedisService.getPlayer(roomId, user1))
                .thenReturn(PlayerStatus.simple(user1, roomId, false));

        // increaseCount 호출 시 answered=true로 바뀌었다고 가정(간단화)
        doAnswer((Answer<Void>) inv -> {
            // 점수 증가 등 내부 동작은 생략
            return null;
        }).when(gameRedisService).increaseCount(roomId, user1);

        // advanceRound 호출 시 라운드 증가/현재 정답 갱신했다고 가정(간단화)
        doAnswer((Answer<Void>) inv -> {
            TrotQuiz nextQuiz = inv.getArgument(1); // TrotQuiz
            state.setRound(2);
            state.setCurrentAnswer("다음정답"); // 그냥 더미
            return null;
        }).when(gameRedisService).advanceRound(eq(roomId), any(TrotQuiz.class));

        // 다음 라운드 문제 더미
        when(trotQuizRepository.findById(20L)).thenReturn(java.util.Optional.of(
                TrotQuiz.builder().id(20L).url("https://y.t/next").build()
        ));

        // --- STOMP 구독 ---
        String wsUrl = "ws://localhost:" + port + "/ws-game";
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch got2 = new CountDownLatch(2); // ANSWER_RESULT + ROUND_QUESTION
        CopyOnWriteArrayList<GameInfoSocketMessage> inbox = new CopyOnWriteArrayList<>();

        StompSessionHandler handler = new StompSessionHandlerAdapter() {
            @Override public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("✅ [TEST] STOMP connected, sessionId=" + session.getSessionId());
                connected.countDown();

                String dest = "/sub/game/" + roomId;
                System.out.println("▶ [TEST] SUBSCRIBE " + dest);

                session.subscribe(dest, new StompFrameHandler() {
                    @Override public java.lang.reflect.Type getPayloadType(StompHeaders headers) { return GameInfoSocketMessage.class; }
                    @Override public void handleFrame(StompHeaders headers, Object payload) {
                        GameInfoSocketMessage msg = (GameInfoSocketMessage) payload;
                        System.out.println("📨 [TEST] RECV type=" + msg.getType() +
                                ", roomId=" + msg.getRoomId() +
                                ", payload='" + msg.getPayload() + "'");
                        inbox.add(msg);
                        got2.countDown();
                    }
                });

                // ★ 트리거: 서비스 메서드 직접 호출(WS 입력 경로 대신)
                System.out.println("▶ [TEST] TRIGGER handleAnswer()");
                gameService.handleAnswer(roomId, user1, correct);
            }
        };

        StompSession sess = stomp.connectAsync(wsUrl, handler).get(5, TimeUnit.SECONDS);
        assertTrue(connected.await(5, TimeUnit.SECONDS));

        // ANSWER_RESULT, ROUND_QUESTION 2개 기다린다
        assertTrue(got2.await(20, TimeUnit.SECONDS));
        System.out.println("[TEST] inbox size=" + inbox.size());
        inbox.forEach(m -> System.out.println("[TEST] got type=" + m.getType() + ", payload=" + m.getPayload()));

        // --- 검증 ---
        var types = inbox.stream().map(GameInfoSocketMessage::getType).toList();
        System.out.println("✅ [TEST] received types=" + types);
        assertTrue(types.contains(GameMessageType.ANSWER_RESULT));
        assertTrue(types.contains(GameMessageType.ROUND_QUESTION));

        var answered = inbox.stream().filter(m -> m.getType()==GameMessageType.ANSWER_RESULT).findFirst().orElseThrow();
        assertEquals(roomId, answered.getRoomId());

        var nextQ = inbox.stream().filter(m -> m.getType()==GameMessageType.ROUND_QUESTION).findFirst().orElseThrow();
        assertEquals(roomId, nextQ.getRoomId());
        assertNotNull(nextQ.getPayload());               // next quiz URL
        assertFalse(nextQ.getPayload().isBlank());

        sess.disconnect();
        System.out.println("🧹 [TEST] session disconnected");
    }

    @Test
    @Timeout(20)
    void answer_wrong_only_rejected() throws Exception {
        long roomId = 2L;
        long user1 = 101L;

        GameStatusRedis state = GameStatusRedis.builder()
                .roomId(roomId).round(1).totalRound(2)
                .currentAnswer("정답") //정답을 정답이라고 설정
                .quizIdList(List.of(10L, 20L)).build();

        when(gameRedisService.getGameStatusRedis(roomId)).thenReturn(state);
        when(gameRedisService.getPlayer(roomId, user1))
                .thenReturn(PlayerStatus.simple(user1, roomId, false));

        //소켓 포트에 연결
        String wsUrl = "ws://localhost:" + port + "/ws-game";
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch got1 = new CountDownLatch(1); // ANSWER_REJECTED만
        CopyOnWriteArrayList<GameInfoSocketMessage> inbox = new CopyOnWriteArrayList<>();

        StompSessionHandler handler = new StompSessionHandlerAdapter() {
            @Override public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                connected.countDown();
                // sub/game/{roomId} 토픽을 구독
                // 이 토픽에서 서버가 보내는 GameInfoSocketMessage를 수신
                session.subscribe("/sub/game/" + roomId, new StompFrameHandler() {
                    @Override public java.lang.reflect.Type getPayloadType(StompHeaders headers) { return GameInfoSocketMessage.class; }
                    @Override public void handleFrame(StompHeaders headers, Object payload) {
                        inbox.add((GameInfoSocketMessage) payload);
                        got1.countDown();
                    }
                });
                //오답을 제출하면
                gameService.handleAnswer(roomId, user1, "오답");
            }
        };

        stomp.connectAsync(wsUrl, handler).get(5, TimeUnit.SECONDS);
        assertTrue(connected.await(5, TimeUnit.SECONDS));
        assertTrue(got1.await(10, TimeUnit.SECONDS));

        var types = inbox.stream().map(GameInfoSocketMessage::getType).toList();
        assertTrue(types.contains(GameMessageType.ANSWER_REJECTED));
        assertFalse(types.contains(GameMessageType.ROUND_QUESTION)); // 오답이면 다음 문제 안감
    }

}
