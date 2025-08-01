package S13P11A708.backend.dto.response.gameRoom;

import S13P11A708.backend.domain.enums.GameStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameRoomResponseDto {
    String roomTitle;
    Integer gameRound;
    String musicEra;
    String category;
    Enum<GameStatus> gameStatus;
    Boolean isCreated;
    String message;
}
