package S13P11A708.backend.domain.redis;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatus {
    private Long userId;
    private int correctCount;
    private boolean hintUsed;

    public void updateHintUsed(boolean hintUsed){
        this.hintUsed = hintUsed;
    }

    public void updateCorrectCount(int correctCount){
        this.correctCount = correctCount;
    }

}
