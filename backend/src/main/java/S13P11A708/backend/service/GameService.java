package S13P11A708.backend.service;

import S13P11A708.backend.domain.GameRoom;
import S13P11A708.backend.domain.TrotQuiz;
import S13P11A708.backend.domain.enums.GameMessageType;
import S13P11A708.backend.dto.redis.GameStatusRedis;
import S13P11A708.backend.dto.redis.PlayerStatus;
import S13P11A708.backend.repository.GameRoomRepository;
import S13P11A708.backend.repository.TrotQuizRepository;
import S13P11A708.backend.repository.UserRepository;
import S13P11A708.backend.service.redis.GameRedisService;
import S13P11A708.backend.websocket.GameBroadcaster;
import S13P11A708.backend.websocket.GameMessageFactory;
import S13P11A708.backend.dto.webSocket.GameAnsSocketMessage;
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
    private final GameMessageFactory messageFactory;

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
        GameAnsSocketMessage startMessage = messageFactory.createMessage(GameMessageType.GAME_START, roomId, "게임이 시작됩니다!");
        GameAnsSocketMessage questionMessage = messageFactory.createMessage(GameMessageType.ROUND_QUESTION, roomId, firstQuiz.getUrl());

        broadcaster.broadcastToRoom(roomId, startMessage);
        broadcaster.broadcastToRoom(roomId, questionMessage);

    }

    /**
     * 정답 인증 처리
     * 방 id, 보낸 사람 id, 보낸 answer
     */
    public void handleAnswer(Long roomId, Long senderId, String answer) {//방 id, 보낸 사람id, 보낸 answer

        //1. 현재 게임상태 불러오기
        GameStatusRedis game = gameRedisService.getGameStatusRedis(roomId);
        if(game == null) throw new RuntimeException("게임 상태를 찾을 수 없습니다.");

        //2. 이미 정답을 맞춘 사람인지
        PlayerStatus player = gameRedisService.getPlayer(roomId, senderId);
        if(player == null) return;

        if(player.isAnswered()){
            broadcaster.sendToUser(senderId,
                    messageFactory.createMessage(GameMessageType.ANSWER_REJECTED, roomId, "이미 정답을 맞추셨습니다."));
            return;
        }

        //3. 정답 여부 판별
        String correctAnswer = game.getCurrentAnswer();
        boolean isCorrect = correctAnswer.equalsIgnoreCase(answer.trim()); //입력받은 string 앞뒤 공백 지우기

        //4. 정답 결과 broadcast
        GameMessageType resultType = isCorrect ? GameMessageType.ANSWER_RESULT : GameMessageType.ANSWER_REJECTED;
        String message = isCorrect ? "정답입니다." : "틀렸습니다.";
        broadcaster.broadcastToRoom(roomId,
                messageFactory.createWithSender(resultType, roomId, senderId, message));

        if(!isCorrect) return;

        //5. 정답 처리: 정답이면 카운트 올리기
        gameRedisService.increaseCount(roomId, senderId);

        //6. 다음 라운드 진행
        int nextRound = game.getRound() + 1;
        if(nextRound > game.getTotalRound()) {
            gameRedisService.finishGame(roomId);
            endGame(roomId, game); // db 저장, 최종 승자 판단
            return;
        }

        //7. 다음 문제 준비
        List<Long> quizIdList = game.getQuizIdList();
        Long nextQuizId = quizIdList.get(nextRound - 1);
        TrotQuiz nextQuiz = trotQuizRepository.findById(nextQuizId)
                .orElseThrow(() -> new RuntimeException("다음 문제를 찾을 수 없습니다."));
        //8.redis 방 상태 업데이트
        gameRedisService.advanceRound(roomId, nextQuiz);

        //9. 다음 문제 클라언트에 전송
        broadcaster.broadcastToRoom(roomId,
                messageFactory.createMessage(GameMessageType.ROUND_QUESTION, roomId, nextQuiz.getUrl()));
    }

    /**
     * 힌트 사용 처리
     */
    public void handleHint(GameAnsSocketMessage message) {
        Long roomId = message.getRoomId();
        Long senderId = message.getSenderId();

        //1. redis에서 사용자 포인트 확인
        //2. 포인트 부족 시 HINT_REJECTED 전송
        //3. 포인트 차감 후, 첫 글자 힌트 전송
    }

    /**
     * 게임을 끝내기
     * db에 redis 정보 저장, 최종 승자 판단
     */
    public void endGame(Long roomId, GameStatusRedis gameRoom){

    }

    /**
     * 시스템 메세지 전송
     */
//    public void sendSystemMessage(Long roomId, String content) {
//        broadcaster.sendSystemMessage(roomId, content);
//    }
}
