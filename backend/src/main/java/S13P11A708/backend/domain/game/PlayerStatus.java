package S13P11A708.backend.domain.game;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "playerStatus", timeToLive = 60)
@Getter
@Builder
@NoArgsConstructor
public class PlayerStatus {

    @Id
    private Long userId;

    private Long roomId; //소속된 방id
    private Long point; //개인 포인트
    private int correctCount;
    private int hintUsedCount;
    private boolean answered;
    private boolean hintUsed;

    public PlayerStatus(Long userId, Long roomId, Long point, int correctCount, int hintUsedCount, boolean answered, boolean hintUsed) {
        this.userId = userId;
        this.roomId = roomId;
        this.point = point;
        this.correctCount = correctCount;
        this.hintUsedCount = hintUsedCount;
        this.answered = answered;
        this.hintUsed = hintUsed;
    }

    public static PlayerStatus simple(Long userId, Long roomId, boolean answered) {
        return new PlayerStatus(userId, roomId, 0L, 0, 0, answered, false);
    }

    public void updateHintUsed(boolean hintUsed){
        this.hintUsed = hintUsed;
    }
    public void updateAnswered(boolean answered){
        this.answered = answered;
    }

    public void updatePoint(Long point){
        this.point = point;
    }

    //뭐지?
    public void updateCorrectCount(int correctCount){
        this.correctCount = correctCount;
    }

    public void addHintUsedCount(int hintUsedCount){
        this.hintUsedCount = hintUsedCount;
    }

    //정답 맞추면
    public void addCorrect() {
        this.correctCount++;
        this.answered = true;
    }

    public void usePoint(Long cost) {
        this.point -= cost;
    }

}
