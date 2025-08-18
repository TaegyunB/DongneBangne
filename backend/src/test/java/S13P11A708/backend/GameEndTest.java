//package S13P11A708.backend;
//
//import S13P11A708.backend.domain.GameHistory;
//import S13P11A708.backend.domain.GameRoom;
//import S13P11A708.backend.domain.User;
//import S13P11A708.backend.domain.enums.GameMessageType;
//import S13P11A708.backend.domain.enums.GameStatus;
//import S13P11A708.backend.domain.game.GameStatusRedis;
//import S13P11A708.backend.domain.game.PlayerStatus;
//import S13P11A708.backend.dto.webSocket.GameEndSocketMessage;
//import S13P11A708.backend.repository.GameHistoryRepository;
//import S13P11A708.backend.repository.GameRoomRepository;
//import S13P11A708.backend.repository.TrotQuizRepository;
//import S13P11A708.backend.repository.UserRepository;
//import S13P11A708.backend.service.GameService;
//import S13P11A708.backend.service.UserService;
//import S13P11A708.backend.service.redis.GameRedisService;
//import S13P11A708.backend.websocket.GameBroadcaster;
//import S13P11A708.backend.websocket.GameMessageFactory;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.stubbing.Answer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.mockito.Mockito.*;
//import static org.assertj.core.api.Assertions.assertThat;
//
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.springframework.boot.test.web.server.LocalServerPort;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class GameEndTest {
//    @LocalServerPort
//    int port;
//
//    @Autowired
//    GameService gameService;
//
//    @Autowired private GameRedisService gameRedisService;
//    @Autowired private GameRoomRepository gameRoomRepository;
//    @Autowired private UserRepository userRepository;
//    @Autowired private UserService userService;
//    @Autowired private GameHistoryRepository gameHistoryRepository;
//    @Autowired private GameBroadcaster broadcaster;
//    @Autowired private GameMessageFactory messageFactory;
//
//    @Test
//    void lastRound_correctAnswer_triggers_endGame_once_and_no_nextRound() {
//        // given
//        long roomId = 99L;
//        long u1 = 1001L, u2 = 1002L;
//
//        // Redis에 저장된 게임 상태 (라운드 == 총라운드)
//        GameStatusRedis game = new GameStatusRedis();
//        game.setRoomId(roomId);
//        game.setRound(3);               // current
//        game.setTotalRound(3);          // total
//        game.setUser1(new PlayerStatus(u1, roomId, 300L, 1, 1, true, true));
//        game.setUser2(new PlayerStatus(u2, roomId, 200L, 3, 2, true, true));
//        when(gameRedisService.getGameStatusRedis(roomId)).thenReturn(game);
//
//        // 종료 전이(레이스 가드) 성공하도록
////        when(gameRedisService.transitionToFinishing(roomId)).thenReturn(true);
////        when(gameRedisService.snapshot(roomId)).thenReturn(game);
//
//        // DB 조회 더미
//        GameRoom room = mock(GameRoom.class);
//        when(gameRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
//        User u1e = mock(User.class), u2e = mock(User.class);
//        when(userRepository.getReferenceById(u1)).thenReturn(u1e);
//        when(userRepository.getReferenceById(u2)).thenReturn(u2e);
//        when(u1e.getSeniorCenter()).thenReturn(null);
//        when(u2e.getSeniorCenter()).thenReturn(null);
//        when(u1e.getNickname()).thenReturn("유저1");
//
//        // end message 더미
//        GameEndSocketMessage endMsg = new GameEndSocketMessage(); // 타입: GAME_END라고 가정
//        when(messageFactory.createEndMessage(
//                eq(GameMessageType.GAME_END), eq(roomId), any(), any(),
//                anyLong(), anyLong(), anyString()
//        )).thenReturn(endMsg);
//
//        // when: 마지막 라운드에서 정답 처리
//        gameService.handleAnswer(roomId, u1, /*정답 텍스트*/ "고향역");
//
//        // then:
//        // 1) 종료 브로드캐스트 1회
//        verify(broadcaster, times(1))
//                .broadcastEndToRoom(eq(roomId), eq(endMsg));
//
//        // 2) finishGame 호출 1회
//        verify(gameRedisService, times(1)).finishGame(roomId);
//
//        // 3) 다음 라운드 전환 미호출 (toNextRound가 여기 이름이라 가정)
////        verify(gameRedisService, never()).advanceRound(anyLong(), any);
//
//        // 4) 상태 플래그 세팅(있다면) 확인
//        verify(gameRoomRepository, times(1)).findById(roomId);
//        verify(gameHistoryRepository, times(1)).save(any(GameHistory.class));
//    }
//
//
//}
