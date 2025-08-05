package S13P11A708.backend.domain.redis;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatus {
    private Long userId;
    private Long rooomId; //소속된 방id
    private int correctCount;
    private boolean hintUsed;
    private Long point; //개인 포인트

    public PlayerStatus(Long userId, int correctCount, boolean hintUsed, Long point) {
    }

    public void updateHintUsed(boolean hintUsed){
        this.hintUsed = hintUsed;
    }

    public void updatePoint(Long point){
        this.point = point;
    }

    public void updateCorrectCount(int correctCount){
        this.correctCount = correctCount;
    }

}
