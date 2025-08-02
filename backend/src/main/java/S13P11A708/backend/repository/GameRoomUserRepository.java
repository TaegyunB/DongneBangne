package S13P11A708.backend.repository;

import S13P11A708.backend.domain.GameRoomUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRoomUserRepository extends JpaRepository<GameRoomUser, Long> {

}
