package S13P11A708.backend;

import S13P11A708.backend.domain.GameRoom;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.domain.enums.GameMessageType;
import S13P11A708.backend.domain.enums.GameStatus;
import S13P11A708.backend.domain.game.GameStatusRedis;
import S13P11A708.backend.domain.game.PlayerStatus;
import S13P11A708.backend.dto.webSocket.GameEndSocketMessage;
import S13P11A708.backend.dto.webSocket.GameInfoSocketMessage;
import S13P11A708.backend.repository.GameHistoryRepository;
import S13P11A708.backend.repository.GameRoomRepository;
import S13P11A708.backend.repository.UserRepository;
import S13P11A708.backend.service.GameService;
import S13P11A708.backend.service.UserService;
import S13P11A708.backend.service.redis.GameRedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameEndSocketTest {
    @LocalServerPort
    int port;

    @Autowired
    GameService gameService;

    // === endGame()에서 접근하는 의존성들 목킹 ===
    @MockitoBean
    GameRedisService gameRedisService;
    @MockitoBean
    GameRoomRepository gameRoomRepository;
    @MockitoBean
    UserRepository userRepository;
    @MockitoBean
    UserService userService;
    @MockitoBean
    GameHistoryRepository gameHistoryRepository;

    @Test
    @Timeout(25)
    void last_round_correct_sends_GAME_END_with_winner() throws Exception {
        long roomId = 99L;
        long u1 = 1001L;
        long u2 = 1002L;
        String correct = "고향역";

        // --- Redis 상태: 마지막 라운드 ---
        PlayerStatus ps1 = new PlayerStatus(u1, roomId, 0L, 3, 1, false, false);
        PlayerStatus ps2 = new PlayerStatus(u2, roomId, 0L, 1, 0, false, false);
        GameStatusRedis state = GameStatusRedis.builder()
                .roomId(roomId).round(3).totalRound(3)
                .currentAnswer(correct)
                .quizIdList(List.of(10L, 20L, 30L))
                .user1(ps1).user2(ps2)
                .build();

        when(gameRedisService.getGameStatusRedis(roomId)).thenReturn(state);
        when(gameRedisService.getPlayer(roomId, u1)).thenReturn(ps1);

        // --- JPA 목킹 ---
        GameRoom roomMock = mock(GameRoom.class);
        when(gameRoomRepository.findById(roomId)).thenReturn(Optional.of(roomMock));

        User user1 = mock(User.class); when(user1.getId()).thenReturn(u1); when(user1.getNickname()).thenReturn("유저1");
        User user2 = mock(User.class); when(user2.getId()).thenReturn(u2); when(user2.getNickname()).thenReturn("유저2");
        when(userRepository.getReferenceById(u1)).thenReturn(user1);
        when(userRepository.getReferenceById(u2)).thenReturn(user2);

        when(gameHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doAnswer(inv -> { System.out.println("   [MOCK] userService.addWinPoint(" + inv.getArgument(0) + ")"); return null; })
                .when(userService).addWinPoint(anyLong());

        // --- STOMP 클라이언트 ---
        WebSocketStompClient stomp = new WebSocketStompClient(new StandardWebSocketClient());
        stomp.setMessageConverter(new MappingJackson2MessageConverter());

        String wsUrl = "ws://localhost:" + port + "/ws-game";
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch gotEnd = new CountDownLatch(1);
        CopyOnWriteArrayList<GameEndSocketMessage> inbox = new CopyOnWriteArrayList<>();

        // RAW 디버그용 (필요 시 켜기)
        final boolean RAW = false;

        StompSessionHandler handler = new StompSessionHandlerAdapter() {
            @Override public void afterConnected(StompSession session, StompHeaders ch) {
                System.out.println("✅ [TEST] connected, sess=" + session.getSessionId());
                connected.countDown();

                String dest = "/sub/games/" + roomId; // 서버 브로드캐스트 경로와 동일
                System.out.println("▶ [TEST] SUB " + dest);

                StompHeaders sh = new StompHeaders();
                sh.setDestination(dest);

                session.subscribe(sh, new StompFrameHandler() {
                    @Override public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
                        return RAW ? byte[].class : GameEndSocketMessage.class;
                    }
                    @Override public void handleFrame(StompHeaders headers, Object payload) {
                        if (RAW) {
                            String raw = new String((byte[]) payload, java.nio.charset.StandardCharsets.UTF_8);
                            System.out.println("📨 RAW: " + raw);
                            try {
                                GameEndSocketMessage parsed = new com.fasterxml.jackson.databind.ObjectMapper()
                                        .readValue(raw, GameEndSocketMessage.class);
                                inbox.add(parsed);
                                if (parsed.getType() == GameMessageType.GAME_END) gotEnd.countDown();
                            } catch (Exception e) {
                                System.out.println("❗ PARSE FAIL: " + e.getMessage());
                            }
                            return;
                        }
                        GameEndSocketMessage msg = (GameEndSocketMessage) payload;
                        System.out.println("📨 [TEST] RECV type=" + msg.getType() + ", payload='" + msg.getPayload() + "'");
                        inbox.add(msg);
                        if (msg.getType() == GameMessageType.GAME_END) gotEnd.countDown();
                    }
                });

                // 🔸 구독 등록 후 약간의 지연을 두고 트리거 (레이스 제거)
                Executors.newSingleThreadScheduledExecutor()
                        .schedule(() -> {
                            System.out.println("▶ [TEST] TRIGGER handleAnswer(correct)");
                            gameService.handleAnswer(roomId, u1, correct);
                        }, 120, TimeUnit.MILLISECONDS);
            }
        };

        StompSession sess = stomp.connectAsync(wsUrl, handler).get(5, TimeUnit.SECONDS);
        assertTrue(connected.await(5, TimeUnit.SECONDS));

        boolean ok = gotEnd.await(15, TimeUnit.SECONDS);
        System.out.println("⏱️ [TEST] await=" + ok + ", inboxSize=" + inbox.size());
        inbox.forEach(m -> System.out.println("🔎 [TEST] inbox: type=" + m.getType() + ", payload='" + m.getPayload() + "'"));
        assertTrue(ok, "GAME_END를 수신하지 못했습니다.");

        GameEndSocketMessage end = inbox.stream()
                .filter(m -> m.getType() == GameMessageType.GAME_END)
                .findFirst().orElseThrow();

        assertEquals(roomId, end.getRoomId());
        assertEquals(u1, end.getWinnerId());
        assertEquals("유저1", end.getWinnerNickname());
        assertNotNull(end.getPayload());
        assertTrue(end.getPayload().contains("승자는"));

        verify(userService, times(1)).addWinPoint(u1);
        verify(gameHistoryRepository, times(1)).save(any());

        sess.disconnect();
        System.out.println("🧹 [TEST] disconnected");
    }

    @Test
    @Timeout(25)
    void last_round_tie_sends_GAME_END_draw() throws Exception {
        long roomId = 100L;
        long u1 = 2001L;
        long u2 = 2002L;
        String correct = "고향역";

        // 동점 상황: correctCount 동일
        PlayerStatus ps1 = new PlayerStatus(u1, roomId, 0L, 2, 0, false, false);
        PlayerStatus ps2 = new PlayerStatus(u2, roomId, 0L, 2, 1, false, false);

        GameStatusRedis state = GameStatusRedis.builder()
                .roomId(roomId).round(2).totalRound(2)
                .currentAnswer(correct)
                .quizIdList(List.of(11L, 22L))
                .user1(ps1).user2(ps2)
                .build();

        when(gameRedisService.getGameStatusRedis(roomId)).thenReturn(state);
        when(gameRedisService.getPlayer(roomId, u1)).thenReturn(ps1);

        GameRoom roomMock = mock(GameRoom.class);
        when(gameRoomRepository.findById(roomId)).thenReturn(Optional.of(roomMock));
        doAnswer(inv -> { System.out.println("   [MOCK] room.changeGameStatus(FINISHED)"); return null; })
                .when(roomMock).changeGameStatus(GameStatus.FINISHED);

        User user1 = mock(User.class); when(user1.getId()).thenReturn(u1); when(user1.getSeniorCenter()).thenReturn(null); when(user1.getNickname()).thenReturn("A");
        User user2 = mock(User.class); when(user2.getId()).thenReturn(u2); when(user2.getSeniorCenter()).thenReturn(null); when(user2.getNickname()).thenReturn("B");
        when(userRepository.getReferenceById(u1)).thenReturn(user1);
        when(userRepository.getReferenceById(u2)).thenReturn(user2);

        when(gameHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WebSocketStompClient stomp = new WebSocketStompClient(new StandardWebSocketClient());
        stomp.setMessageConverter(new MappingJackson2MessageConverter());

        String wsUrl = "ws://localhost:" + port + "/ws-game";
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch gotEnd = new CountDownLatch(1);
        CopyOnWriteArrayList<GameEndSocketMessage> inbox = new CopyOnWriteArrayList<>();

        StompSessionHandler handler = new StompSessionHandlerAdapter() {
            @Override public void afterConnected(StompSession session, StompHeaders ch) {
                System.out.println("✅ [TEST] connected (tie)");
                connected.countDown();
                //웹소켓 구독 (서버 브로드캐스트를 받음)
                String dest = "/sub/games/" + roomId;
                System.out.println("▶ [TEST] SUB " + dest);
                session.subscribe(dest, new StompFrameHandler() {
                    @Override public java.lang.reflect.Type getPayloadType(StompHeaders headers) { return GameEndSocketMessage.class; }
                    @Override public void handleFrame(StompHeaders headers, Object payload) {
                        GameEndSocketMessage msg = (GameEndSocketMessage) payload;
                        System.out.println("📨 [TEST] RECV " + msg.getType() + " '" + msg.getPayload() + "'");
                        inbox.add(msg);
                        gotEnd.countDown();
                    }
                });
                System.out.println("▶ [TEST] TRIGGER handleAnswer(correct, tie)");
                gameService.handleAnswer(roomId, u1, correct);
            }
        };

        stomp.connectAsync(wsUrl, handler).get(5, TimeUnit.SECONDS);
        assertTrue(connected.await(5, TimeUnit.SECONDS));
        assertTrue(gotEnd.await(15, TimeUnit.SECONDS));

        GameEndSocketMessage end = inbox.stream()
                .filter(m -> m.getType() == GameMessageType.GAME_END)
                .findFirst().orElseThrow();

        assertEquals(roomId, end.getRoomId());
        assertTrue(end.getPayload().contains("무승부")); // 무승부 메시지 확인
        verify(userService, never()).addWinPoint(anyLong()); // 동점이면 포인트 부여 없음
    }
}
