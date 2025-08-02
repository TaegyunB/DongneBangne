package S13P11A708.backend.repository;

import S13P11A708.backend.domain.GameRoomUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRoomUserRepository extends JpaRepository<GameRoomUser, Long> {
    /**
     게임방에 참가한 유저의 정보, 준비 상태를
     GameRoom <-> User 간 관리
     특정 방에 유저가 참여했는지 여부 확인
     **/
    Optional<GameRoomUser> findByGameRoomId_IdAndUserId_Id(Long gameRoomId, Long userId);

    /**
     * 방 나가기 기능
     * 유저가 방에서 나가려고 하면 해당 기록 삭제
     */
    void deleteByGameRoomId_IdAndUserId_Id(Long gameRoomId, Long userId);

    /**
     * 해당 방에 몇명의 유저가 참가 중인지
     * 게임 시작 조건(2명 이상) 파악
     */
    int countByGameRoomId_Id(Long gameRoomId);

    /**
     * 해당 방에 있는 참가자 조회
     */
    List<GameRoomUser> findAllByGameRoomId_Id(Long gameRoomId);
}
