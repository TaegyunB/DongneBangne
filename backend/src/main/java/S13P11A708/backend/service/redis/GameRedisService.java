package S13P11A708.backend.service.redis;

import S13P11A708.backend.domain.TrotQuiz;
import S13P11A708.backend.domain.enums.GameStatus;
import S13P11A708.backend.domain.game.GameStatusRedis;
import S13P11A708.backend.domain.game.PlayerStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final int HINT_COST = 20;
    //우승 시 100

    private String getKey(Long roomId) {
        return "game:room:" + roomId;
    }

    /**
     * roomId를 Redis key로 변환하여, 게임 상태 전체를 redis에 저장
     * 60분이 지나면 자동으로 삭제되도록 설정
     */
    public void saveGameStatus(Long roomId, GameStatusRedis status) {
        redisTemplate.opsForValue().set(getKey(roomId), status, Duration.ofMinutes(60));
    }

    /**
     * 게임이 끝나면, Redis에 저장된 게임 상태 데이터 삭제
     */
    public void deleteGameStatus(Long roomId) {
        redisTemplate.delete(getKey(roomId));
    }

    /**
     * roomId로 GameStatusRedis(게임방 정보) 가져오기
     */
    public GameStatusRedis getGameStatusRedis(Long roomId) {
        Object data = redisTemplate.opsForValue().get(getKey(roomId));

        if (data instanceof GameStatusRedis) {
            return (GameStatusRedis) data;
        }

        if (data instanceof Map) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(data, GameStatusRedis.class);
        }

        return null;
    }

    /**
     * websocket에서 게임 시작 메세지 받으면,
     * db에서 redis로 초기 데이터 전송 진행해야 됨.
     */
    public void initGame(Long roomId, int totalRound, Long userId1, Long point1, Long userId2, Long point2, List<Long> quizIdList, TrotQuiz firstQuiz) {
        GameStatusRedis status = GameStatusRedis.builder()
                .roomId(roomId)
                .round(1)
                .totalRound(totalRound)
                .quizIdList(quizIdList)
                .currentQuizId(firstQuiz.getId())
                .currentAnswer(firstQuiz.getAnswer())
                .currentUrl(firstQuiz.getUrl())
                .status(GameStatus.PROGRESS)
                .user1(new PlayerStatus(userId1, roomId, point1, 0, 0, false, false))
                .user2(new PlayerStatus(userId2, roomId, point2, 0, 0, false, false))
                .build();

        saveGameStatus(roomId, status);
    }


    /**
     * redis 게임방 정보에 들어 있는 user 정보 get
     */
    public PlayerStatus getPlayer(Long roomId, Long senderId){
        GameStatusRedis status = getGameStatusRedis(roomId);
        return status.getPlayerStatus(senderId);
    }

    /**
     * 유저가 맞춘 문제 갯수 올리기
     */
    public void increaseCount(Long roomId, Long userId) {
        GameStatusRedis status = getGameStatusRedis(roomId);
        if(status == null) return;

        if (status != null) {
            if (status.getUser1().getUserId().equals(userId)) {
                status.getUser1().updateCorrectCount(status.getUser1().getCorrectCount() + 1);
            } else if (status.getUser2().getUserId().equals(userId)) {
                status.getUser2().updateCorrectCount(status.getUser2().getCorrectCount() + 1);
            }
            saveGameStatus(roomId, status);
        }
    }

    /**
     * 힌트 사용 기록
     * hint 사용한 유저는 사용했음을 게임상태 redis에 표시
     */
    public void markHintUsed(Long roomId, Long userId) {
        GameStatusRedis status = getGameStatusRedis(roomId);
        if(status == null) return;

        PlayerStatus player = findPlayer(status, userId);
        if (player == null || player.isHintUsed()) return;

        if(player != null && !player.isHintUsed()){
            player.updateHintUsed(true);
            player.addHintUsedCount(player.getHintUsedCount()+1);
            saveGameStatus(roomId, status);
        }
    }

    /**
     * 힌트를 사용할 수 있는 상태인지 확인
     * 개인 포인트가 충분한지
     */
    public boolean canUseHint(Long roomId, Long userId) {
        GameStatusRedis status = getGameStatusRedis(roomId);
        if(status == null) return false;

        PlayerStatus player = findPlayer(status, userId);
        if(player == null) return false;

        return !player.isHintUsed() && player.getPoint() >= HINT_COST;
    }

    /**
     * 힌트 사용시, player point 차감
     */
    public boolean deductPointForHint(Long roomId, Long userId){
        GameStatusRedis status = getGameStatusRedis(roomId);
        if(status == null) return false;

        PlayerStatus player = findPlayer(status, userId);
        if(player == null) return false;
        if(player.isHintUsed()) return false;
        if(player.getPoint() < HINT_COST) return false;

        player.updatePoint(player.getPoint() - HINT_COST); //힌트 포인트 차감
        saveGameStatus(roomId, status);
        return true;
    }

    /**
     * player 정보 상태 불러오기
     */
    private PlayerStatus findPlayer(GameStatusRedis status, Long userId){
        if(status.getUser1().getUserId().equals(userId)) return status.getUser1();
        if(status.getUser2().getUserId().equals(userId)) return status.getUser2();
        return null;
    }

    /**
     * 해당 라운드가 끝나면 redis를 다음 라운드에 맞게 수정
     * hint 사용 여부 초기화
     * round 수 +1 증가
     */
    public void advanceRound(Long roomId, TrotQuiz nextQuiz) {
        GameStatusRedis status = getGameStatusRedis(roomId);
        if (status != null) {
            status.nextRound(status.getRound() + 1);
            status.updateCurrentQuizId(nextQuiz.getId());
            status.updateCurrentAnswer(nextQuiz.getAnswer());
            status.updateCurrentUrl(nextQuiz.getUrl());
            status.getUser1().updateHintUsed(false);
            status.getUser1().updateAnswered(false);
            status.getUser2().updateHintUsed(false);
            status.getUser2().updateAnswered(false);
            saveGameStatus(roomId, status);
        }
    }

    /**
     * 게임이 종료되면 게임상태를 finished로 바꾼다.
     */
    public void finishGame(Long roomId) {
        GameStatusRedis status = getGameStatusRedis(roomId);
        if (status != null) {
            status.updateStatus(GameStatus.FINISHED);
            saveGameStatus(roomId, status);
        }
        //redis 정보 삭제
        String key = getKey(roomId);
        redisTemplate.delete(key);
    }


}
