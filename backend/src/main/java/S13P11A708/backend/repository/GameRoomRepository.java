package S13P11A708.backend.repository;

import S13P11A708.backend.domain.GameRoom;
import S13P11A708.backend.domain.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    /**게임방의 상태별로 필터링
     * WAITING, PROGESS, FINISHED 중 특정 방 조회할 때 사용
     * 현재 참여 가능한 방 목록 조회 시 사용
     */
    List<GameRoom> findByGameStatus(GameStatus status);
}
