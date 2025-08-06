package S13P11A708.backend.service;

import S13P11A708.backend.domain.GameRoom;
import S13P11A708.backend.domain.TrotQuiz;
import S13P11A708.backend.domain.enums.GameMessageType;
import S13P11A708.backend.dto.redis.PlayerStatus;
import S13P11A708.backend.repository.GameRoomRepository;
import S13P11A708.backend.repository.TrotQuizRepository;
import S13P11A708.backend.repository.UserRepository;
import S13P11A708.backend.service.redis.GameRedisService;
import S13P11A708.backend.websocket.GameBroadcaster;
import S13P11A708.backend.websocket.GameSocketMessage;
import S13P11A708.backend.websocket.GameSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final UserRepository userRepository;
    private final GameRedisService gameRedisService;
    private final GameBroadcaster broadcaster;
    private final TrotQuizRepository trotQuizRepository;
    private final GameRoomRepository gameRoomRepository;

    /**
     * 게임 시작하면, 각 유저의 개인 포인트를 db에서 가져와서
     * redis 방정보 초기화
     * 게임 참가자 정보 초기화
     * 게임 시작 처리 -> 웹소켓으로 정보 전달
     */
    public void startGame(Long roomId, Long user1Id, Long user2Id) {
        // 1. 게임방 정보 조회
        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow(()-> new RuntimeException("게임방이 존재하지 않습니다."));

        int totalRounds = room.getGameRound();
        String musicEra = room.getMusicEra();
        String category = room.getCategory();

        //2. 각 참가자 포인트 조회
        Long point1 = userRepository.findPointByUserId(user1Id);
        Long point2 = userRepository.findPointByUserId(user2Id);

        //3. 방설정 조건 기반 trotQuiz 추출
        List<TrotQuiz> quizList = trotQuizRepository.findRandomQuestionsByCondition(
                musicEra, category, totalRounds
        );
        if(quizList.size() < totalRounds){
            throw new RuntimeException("조건에 맞는 트로트 퀴즈가 부족합니다.");
        }

        //4. 퀴즈 ID 리스트 + 첫번째 문제 정보 구성
        List<Long> quizIdList = quizList.stream()
                .map(TrotQuiz::getId)
                .toList();

        TrotQuiz firstQuiz = quizList.get(0);

        // 5. Redis에 게임 상태, playe1/2 상태 보내기
        gameRedisService.initGame(roomId, totalRounds, user1Id, point1, user2Id, point2, quizIdList, firstQuiz);

        // 6. WebSocket을 통해 참가자들에게 GAME_START 및 ROUND_QUESTION 메시지 전송
        GameSocketMessage startMessage = GameSocketMessage.builder()
                .type(GameMessageType.GAME_START)
                .roomId(roomId)
                .payload("게임이 시작됩니다!")
                .build();

        GameSocketMessage questionMessage = GameSocketMessage.builder()
                .type(GameMessageType.ROUND_QUESTION)
                .roomId(roomId)
                .payload(firstQuiz.getUrl())
                .build();

        broadcaster.broadcastToRoom(roomId, startMessage);
        broadcaster.broadcastToRoom(roomId, questionMessage);

    }

    /**
     * 정답 제출 처리
     */
    public void handleAnswer(GameSocketMessage message) {
        Long roomId = message.getRoomId();
        Long senderId = message.getSenderId();
        String submittedAnswer = message.getPayload();

        //1. redis에서 현재 라운드 문제 정답 확인
        //2. 이미 맞춘 사람인지 확인
        //3. 정답 여부 판별
        //4. 결과 broadcast
        //5. 정답이면 포인트 지금, 다음 라운드 전환
    }

    /**
     * 힌트 사용 처리
     */
    public void handleHint(GameSocketMessage message) {
        Long roomId = message.getRoomId();
        Long senderId = message.getSenderId();

        //1. redis에서 사용자 포인트 확인
        //2. 포인트 부족 시 HINT_REJECTED 전송
        //3. 포인트 차감 후, 첫 글자 힌트 전송
    }

    /**
     * 시스템 메세지 전송
     */
//    public void sendSystemMessage(Long roomId, String content) {
//        broadcaster.sendSystemMessage(roomId, content);
//    }
}
