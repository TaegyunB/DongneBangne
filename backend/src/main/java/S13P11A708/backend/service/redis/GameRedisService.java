package S13P11A708.backend.service.redis;

import S13P11A708.backend.domain.enums.GameStatus;
import S13P11A708.backend.domain.redis.GameStatusRedis;
import S13P11A708.backend.domain.redis.PlayerStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
     * roomId로 게임상태를 Redis에서 조회
     */
    public GameStatusRedis getGameStatus(Long roomId) {
        Object data = redisTemplate.opsForValue().get(getKey(roomId));
        return (data instanceof GameStatusRedis) ? (GameStatusRedis) data : null;
    }

    /**
     * 게임이 끝나면, Redis에 저장된 게임 상태 데이터 삭제
     */
    public void deleteGameStatus(Long roomId) {
        redisTemplate.delete(getKey(roomId));
    }

    /**
     * roomId로 GameStatusRedis 가져오기
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
     * db에서 redis로 데이터 초기화 진행해야 됨.
     */
    public void initGame(Long roomId, int totalRound, Long userId1, Long point1, Long userId2, Long point2) {
        GameStatusRedis status = GameStatusRedis.builder()
                .roomId(roomId)
                .round(1)
                .totalRound(totalRound)
                .currentQuestionId(null)
                .status(GameStatus.PROGRESS)
                .user1(new PlayerStatus(userId1, 0, false, point1))
                .user2(new PlayerStatus(userId2, 0, false, point2))
                .build();

        saveGameStatus(roomId, status);
    }


    /**
     * 현재 라운드에 해당하는 문제의 id를 redis 상태에 반영
     */
    public void updateCurrentQuestion(Long roomId, Long questionId) {
        GameStatusRedis status = getGameStatusRedis(roomId);
        if (status != null) {
            status.updateCurrentQuestionId(questionId);
            saveGameStatus(roomId, status);
        }
    }

    /**
     * 유저가 맞춘 문제 갯수 올리기
     */
    public void increaseScore(Long roomId, Long userId) {
        GameStatusRedis status = getGameStatusRedis(roomId);
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
     * hint 사용한 유저는 사용했음을 게임상태 redis에 표시
     */
    public void markHintUsed(Long roomId, Long userId) {
        GameStatusRedis status = getGameStatus(roomId);
        if (status != null) {
            if (status.getUser1().getUserId().equals(userId)) {
                status.getUser1().updateHintUsed(true);
            } else if (status.getUser2().getUserId().equals(userId)) {
                status.getUser2().updateHintUsed(true);
            }
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
        return player != null && player.getPoint() >= HINT_COST;
    }

    /**
     * 힌트 사용시, player point 차감
     */
    public void useHint(Long roomId, Long userId){
        GameStatusRedis status = getGameStatusRedis(roomId);
        if(status == null) return;

        PlayerStatus player = findPlayer(status, userId);
        if(player != null && canUseHint(roomId, userId)) {
            player.updateHintUsed(true);
            player.updatePoint(player.getPoint() - HINT_COST); //힌트 포인트 차감
            saveGameStatus(roomId, status);
        }
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
    public void advanceRound(Long roomId) {
        GameStatusRedis status = getGameStatus(roomId);
        if (status != null) {
            status.nextRound(status.getRound() + 1);
            status.getUser1().updateHintUsed(false);
            status.getUser2().updateHintUsed(false);
            saveGameStatus(roomId, status);
        }
    }

    /**
     * 게임이 종료되면 게임상태를 finished로 바꾼다.
     */
    public void finishGame(Long roomId) {
        GameStatusRedis status = getGameStatus(roomId);
        if (status != null) {
            status.updateStatus(GameStatus.FINISHED);
            saveGameStatus(roomId, status);
        }
    }

}
