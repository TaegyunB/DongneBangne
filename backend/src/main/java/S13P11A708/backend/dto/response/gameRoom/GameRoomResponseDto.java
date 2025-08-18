package S13P11A708.backend.dto.response.gameRoom;

import S13P11A708.backend.domain.GameRoom;
import S13P11A708.backend.domain.enums.GameStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class GameRoomResponseDto {
    /**
     * WAITING 상태인 게임방 조회할 때 사용되는 response body
     */
    Long id;
    String roomTitle;
    Integer gameRound;
    String gameStatus;
    int participantCount; //방 속 참가자 수
    LocalDateTime createdAt;

    public static GameRoomResponseDto from(GameRoom room){
        return GameRoomResponseDto.builder()
                .id(room.getId())
                .roomTitle(room.getRoomTitle())
                .gameRound(room.getGameRound())
                .gameStatus(room.getGameStatus().name())
                .build();
    }
}
