package S13P11A708.backend;

import S13P11A708.backend.domain.GameRoom;
import S13P11A708.backend.domain.GameRoomUser;
import S13P11A708.backend.domain.TrotQuiz;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.domain.enums.GameMessageType;
import S13P11A708.backend.domain.enums.GameStatus;
import S13P11A708.backend.repository.GameRoomRepository;
import S13P11A708.backend.repository.GameRoomUserRepository;
import S13P11A708.backend.repository.TrotQuizRepository;
import S13P11A708.backend.repository.UserRepository;
import S13P11A708.backend.service.GameService;
import S13P11A708.backend.dto.webSocket.GameInfoSocketMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameStartSocketTest {
    @LocalServerPort
    int port;

    @Autowired
    GameService gameService;

    @Autowired
    GameRoomRepository gameRoomRepository;
    @Autowired
    GameRoomUserRepository gameRoomUserRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TrotQuizRepository trotQuizRepository;

    long roomId;
    long user1;
    long user2;

    WebSocketStompClient stomp;

    @BeforeEach
    void setUp() {
        stomp = new WebSocketStompClient(new StandardWebSocketClient());
        stomp.setMessageConverter(new MappingJackson2MessageConverter());

        // 1) 유저 두 명 (포인트 포함)
        User u1 = userRepository.save(User.builder().id(null).nickname("u1").personalPoint(100L).build());
        User u2 = userRepository.save(User.builder().id(null).nickname("u2").personalPoint(200L).build());
        userRepository.flush();
        user1 = u1.getId();
        user2 = u2.getId();

        // 2) 게임방 (라운드/시대/카테고리 설정)
        GameRoom room = gameRoomRepository.save(GameRoom.builder()
                .gameRound(2)               // 테스트에 필요한 만큼
                .gameStatus(GameStatus.WAITING)
                .build());
        gameRoomRepository.flush();
        roomId = room.getId();

        // 3) 참가자 매핑 2명
        gameRoomUserRepository.save(GameRoomUser.builder()
                .gameRoomId(room).userId(u1).ready(false).build());
        gameRoomUserRepository.save(GameRoomUser.builder()
                .gameRoomId(room).userId(u2).ready(false).build());
        gameRoomUserRepository.flush();

        // 4) 퀴즈 최소 2개 (조건 일치)
        trotQuizRepository.save(TrotQuiz.builder().url("https://y.t/1").build());
        trotQuizRepository.save(TrotQuiz.builder().url("https://y.t/2").build());
        trotQuizRepository.flush();
    }

    @Test
    @Timeout(20)
    void startGame_socket_2_user() throws Exception {
        System.out.println("▶ TEST-START: wsUrl=ws://localhost:" + port + "/ws-game"
                + ", roomId=" + roomId + ", user1=" + user1 + ", user2=" + user2);

        String wsUrl = "ws://localhost:" + port + "/ws-game";
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch received2 = new CountDownLatch(2);
        List<GameInfoSocketMessage> inbox = new CopyOnWriteArrayList<>();
        AtomicInteger order = new AtomicInteger(0);

        StompSessionHandler handler = new StompSessionHandlerAdapter() {
            @Override public void afterConnected(StompSession session, StompHeaders ch) {
                connected.countDown();

                // ★ broadcaster가 실제로 보내는 목적지와 동일하게!
                // 예: broadcaster.broadcastToRoom(roomId, msg) 가 "/sub/games/{roomId}" 로 보낸다면:
                String dest = "/sub/games/" + roomId;
                System.out.println("[TEST] subscribe dest = " + dest);

                session.subscribe(dest, new StompFrameHandler() {
                    @Override public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
                        return GameInfoSocketMessage.class; // 서버가 보내는 DTO
                    }
                    @Override public void handleFrame(StompHeaders headers, Object payload) {
                        System.out.println("[TEST] received -> " + payload);
                        inbox.add((GameInfoSocketMessage) payload);
                        order.incrementAndGet();
                        received2.countDown();
                    }
                });

                try {
                    // ★ 테스트 트리거: 서비스 직접 호출
                    gameService.startGame(roomId, user1, user2);
                    System.out.println("[TEST] startGame called");
                } catch (Throwable t) {
                    t.printStackTrace();
                }

            }
        };

        // CONNECT
        CompletableFuture<StompSession> f = stomp.connectAsync(wsUrl, handler);
        StompSession session = f.get(5, TimeUnit.SECONDS);
        System.out.println("✅ CONNECT-OK: sessionId=" + session.getSessionId());
        assertTrue(connected.await(5, TimeUnit.SECONDS));
        System.out.println("✅ LATCH connected OK");

        // 두 개 메시지(GAME_START, ROUND_QUESTION) 기다림
        boolean gotTwo = received2.await(30, TimeUnit.SECONDS);
        System.out.println("⏱️ WAIT-MSG done: inboxSize=" + inbox.size());
        assertTrue(gotTwo);

        // 검증
        assertEquals(2, inbox.size());

        //수신한 모든 메세지 타입 추출
        var types = inbox.stream().map(GameInfoSocketMessage::getType).toList();
        System.out.println("🔎 ASSERT: types=" + types);
        //GAME_START 메세지가 있었는지
        assertTrue(types.contains(GameMessageType.GAME_START));
        assertTrue(types.contains(GameMessageType.ROUND_QUESTION));

// START 메시지
        var start = inbox.stream()
                .filter(m -> m.getType() == GameMessageType.GAME_START)
                .findFirst().orElseThrow();

        assertEquals(roomId, start.getRoomId());
        assertNotNull(start.getPayload());
        assertFalse(start.getPayload().isBlank());

// QUESTION 메시지
        var question = inbox.stream()
                .filter(m -> m.getType() == GameMessageType.ROUND_QUESTION)
                .findFirst().orElseThrow();

        assertEquals(roomId, question.getRoomId());
        assertNotNull(question.getPayload());
        assertFalse(question.getPayload().isBlank()); // 보통 firstQuiz URL

        session.disconnect();
        System.out.println("🧹 DISCONNECT session");
    }
}
