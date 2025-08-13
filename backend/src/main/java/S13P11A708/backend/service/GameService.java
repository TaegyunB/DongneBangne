package S13P11A708.backend.service;

import S13P11A708.backend.domain.*;
import S13P11A708.backend.domain.enums.GameMessageType;
import S13P11A708.backend.domain.enums.GameStatus;
import S13P11A708.backend.domain.game.GameStatusRedis;
import S13P11A708.backend.domain.game.PlayerStatus;
import S13P11A708.backend.dto.webSocket.GameInfoSocketMessage;
import S13P11A708.backend.repository.GameHistoryRepository;
import S13P11A708.backend.repository.GameRoomRepository;
import S13P11A708.backend.repository.TrotQuizRepository;
import S13P11A708.backend.repository.UserRepository;
import S13P11A708.backend.service.redis.GameRedisService;
import S13P11A708.backend.websocket.GameBroadcaster;
import S13P11A708.backend.websocket.GameMessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final UserRepository userRepository;
    private final GameRedisService gameRedisService;
    private final GameBroadcaster broadcaster;
    private final TrotQuizRepository trotQuizRepository;
    private final GameRoomRepository gameRoomRepository;
    private final GameMessageFactory messageFactory;
    private final UserService userService;
    private final GameHistoryRepository gameHistoryRepository;

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

        //2. 각 참가자 포인트 조회
        Long point1 = userRepository.findPointByUserId(user1Id);
        Long point2 = userRepository.findPointByUserId(user2Id);

        //3. 방설정 조건 기반 trotQuiz 추출
        List<TrotQuiz> quizList = trotQuizRepository.findRandomQuestions(
                totalRounds
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
        GameInfoSocketMessage startMessage = messageFactory.createInfoMessage(GameMessageType.GAME_START, roomId, "게임시작");
        GameInfoSocketMessage questionMessage = messageFactory.createInfoMessage(GameMessageType.ROUND_QUESTION, roomId, firstQuiz.getUrl());

        broadcaster.broadcastToRoom(roomId, startMessage);
        broadcaster.broadcastToRoom(roomId, questionMessage);

    }

    /**
     * 정답 인증 처리
     * 방 id, 보낸 사람 id, 보낸 answer
     */
    public void handleAnswer(Long roomId, Long senderId, String answer) {//방 id, 보낸 사람id, 보낸 answer

        log.info("[FLOW] ===== handleAnswer START =====");
        log.info("[1] 방 ID={}, 제출자 ID={}, 제출 답안='{}'", roomId, senderId, answer);

        //1. 현재 게임상태 불러오기
        GameStatusRedis game = gameRedisService.getGameStatusRedis(roomId);
        if(game == null) throw new RuntimeException("게임 상태를 찾을 수 없습니다.");

        log.info("[2] 현재 라운드={}, 총 라운드={}, 정답='{}'",
                game.getRound(), game.getTotalRound(), game.getCurrentAnswer());

        //2. 이미 정답을 맞춘 사람인지
        PlayerStatus player = gameRedisService.getPlayer(roomId, senderId);
        log.info("[3] 플레이어 상태: answered={}, correctCount={}",
                player.isAnswered(), player.getCorrectCount());

        if(player == null) return;

        if(player.isAnswered()){
            log.info("[FLOW] 이미 정답 맞춘 상태 → 리턴");
            broadcaster.broadcastToRoom(senderId,
                    messageFactory.createInfoMessage(GameMessageType.ANSWER_REJECTED, roomId, "이미 정답을 맞추셨습니다"));
            return;
        }

        //3. 정답 여부 판별
        String correctAnswer = game.getCurrentAnswer();
        boolean isCorrect = correctAnswer.equalsIgnoreCase(answer.trim()); //입력받은 string 앞뒤 공백 지우기
        log.info("[4] 정답 여부 → {}", isCorrect);

        if (!isCorrect) {
            broadcaster.broadcastAns(roomId,
                    messageFactory.createAnsMessage(GameMessageType.ANSWER_REJECTED, roomId, false));
            log.info("[FLOW] 오답 → 라운드 유지");
            return;
        }

        //4. 정답 처리
        gameRedisService.increaseCount(roomId, senderId);
        broadcaster.broadcastAns(roomId,
                messageFactory.createAnsMessage(GameMessageType.ANSWER_RESULT, roomId, true));
        log.info("[5] 정답자 카운트 증가");

        //5. 다음 라운드 진행
        boolean isLastRound = game.getRound() >= game.getTotalRound();
        if(isLastRound) {
            log.info("[FLOW] 마지막 라운드 → 게임 종료 처리");
            gameRedisService.finishGame(roomId);
            log.info("[FLOW] broadcasting GAME_END to /sub/game/"+ roomId);
            endGame(roomId, game); // db 저장, 최종 승자 판단
            return;
        }

        //7. 다음 문제 준비
        int nextRound = game.getRound() + 1;
        List<Long> quizIdList = game.getQuizIdList();
        Long nextQuizId = quizIdList.get(nextRound - 1);
        TrotQuiz nextQuiz = trotQuizRepository.findById(nextQuizId)
                .orElseThrow(() -> new RuntimeException("다음 문제를 찾을 수 없습니다."));

        //8.redis 방 상태 업데이트
        gameRedisService.advanceRound(roomId, nextQuiz);
        log.info("[6] 다음 라운드={}, 다음 문제 ID={}", nextRound, nextQuizId);

        //9. 다음 문제 클라언트에 전송
        broadcaster.broadcastToRoom(roomId,
                messageFactory.createInfoMessage(GameMessageType.ROUND_QUESTION, roomId, nextQuiz.getUrl()));

        log.info("[7] 다음 문제 URL 방송: {}", nextQuiz.getUrl());

        log.info("===== [handleAnswer END] =====");
    }

    /**
     * 힌트 사용 처리
     */
    public void handleHint(Long roomId, Long userId) {
        //레디스에서 게임정보 가져오기
        GameStatusRedis game = gameRedisService.getGameStatusRedis(roomId);
        if(game == null) throw new RuntimeException("게임 상태를 찾을 수 없습니다.");

        //1. 이미 힌트 사용 가능한지(포인트, 사용여부)
        if(!gameRedisService.canUseHint(roomId, userId)){
            broadcaster.sendToUser(userId,
                    messageFactory.createHintMessage(GameMessageType.HINT_REJECTED, roomId, userId, false, "힌트를 더 이상 사용할 수 없습니다."));
            return;
        }

        //2. 포인트 차감 시도
        boolean deducted = gameRedisService.deductPointForHint(roomId, userId);
        if(!deducted){
            log.info("[HINT] NO_POINT: send -> /queue/hint/{}, msg='포인트가 부족합니다.'", userId);
            broadcaster.sendToUser(userId,
                    messageFactory.createHintMessage(GameMessageType.HINT_REJECTED, roomId, userId, false, "포인트가 부족합니다."));
            return;
        }

        //3. 힌트 사용 기록
        gameRedisService.markHintUsed(roomId, userId);

        //4. 첫 글자 힌트 전송
        String answer = game.getCurrentAnswer();
        String hint = answer.substring(0,1);

        broadcaster.sendToUser(userId,
                messageFactory.createHintMessage(GameMessageType.HINT_RESPONSE, roomId, userId, true, hint));
    }

    /**
     * 게임을 끝내기
     * db에 redis 정보 저장, 최종 승자 판단
     */
    public void endGame(Long roomId, GameStatusRedis game){
        log.info("[END] >>> enter endGame(roomId={})", roomId);

        PlayerStatus user1 = game.getUser1();
        PlayerStatus user2 = game.getUser2();
        log.info("[END] Player1: id={}, correctCount={}", user1.getUserId(), user1.getCorrectCount());
        log.info("[END] Player2: id={}, correctCount={}", user2.getUserId(), user2.getCorrectCount());

        int count1 = user1.getCorrectCount();
        int count2 = user2.getCorrectCount();

        Long winnerId = null;
        if(count1>count2) {
            winnerId = user1.getUserId();
        }else if(count1<count2){
            winnerId = user2.getUserId();
        }

        log.info("[END] winnerId={}", winnerId);

        //1. 포인트 부여 (무승부일 경우 생략)
        if(winnerId != null){
            log.info("[END] addWinPoint(winnerId={}) 호출", winnerId);
            userService.addWinPoint(winnerId);
        }

        //2. 엔티티 조회
        log.info("[END] find GameRoom from DB");
        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("게임방을 찾을 수 없습니다."));
        log.info("[END] GameRoom 조회 성공");

        log.info("[END] find Users from DB");
        User user1Entity = userRepository.getReferenceById(user1.getUserId());
        User user2Entity = userRepository.getReferenceById(user2.getUserId());
        log.info("[END] User 조회 성공");

        User winnerEntity = (winnerId != null)
                ? (winnerId.equals(user1.getUserId()) ? user1Entity : user2Entity)
                : null;

        //3-1. 게임 결과 기록 GameHistory
        log.info("[END] save GameHistory");
        GameHistory history = GameHistory.of(
                room,
                winnerEntity,
                game.getStartedAt(),
                LocalDateTime.now(),
                game.getTotalRound(),
                room.getMusicEra(),
                room.getCategory()
        );

        //3-2. 게임 결과 기록 GameHistoryUser
        log.info("[END] save GameHistoryUser1,2");
        List<GameHistoryUser> historyUsers = List.of(
                GameHistoryUser.of(
                        history,
                        user1Entity,
                        user1Entity.getSeniorCenter(),
                        user1.getCorrectCount(),
                        user1.getHintUsedCount(), // PlayerStatus에서 가져와야 함
                        winnerId != null && winnerId.equals(user1.getUserId()
                        )
                ),
                GameHistoryUser.of(
                        history,
                        user2Entity,
                        user2Entity.getSeniorCenter(),
                        user2.getCorrectCount(),
                        user2.getHintUsedCount(),
                        winnerId != null && winnerId.equals(user2.getUserId())
                )
        );
        log.info("[END] GameHistoryUser 저장 완료");
        historyUsers.forEach(history::addHistoryUser);

        //3. 저장
        gameHistoryRepository.save(history);
        room.changeGameStatus(GameStatus.FINISHED);
        log.info("[END] GameRoom 상태 변경 -> FINISHED");

        //4. webSocket 알림 전송
        String winnerNickname = (winnerEntity != null) ? winnerEntity.getNickname() : null;
        String resultMessage = (winnerEntity != null)
                ? "승자는 " + winnerNickname + " 님입니다"
                : "무승부입니다";

        log.info("[END] broadcasting GAME_END to /sub/games/{}", roomId);
        broadcaster.broadcastEndToRoom(roomId,
                messageFactory.createEndMessage(GameMessageType.GAME_END, roomId, winnerId, winnerNickname,
                        user1.getPoint(), user2.getPoint(), resultMessage));

        //5. redis 정리
        gameRedisService.finishGame(roomId);
        log.info("[END] finishGame(roomId) done");
    }

}
