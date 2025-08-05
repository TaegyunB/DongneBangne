package S13P11A708.backend.service;

import S13P11A708.backend.repository.UserRepository;
import S13P11A708.backend.service.redis.GameRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {

    private final UserRepository userRepository;
    private final GameRedisService gameRedisService;

    /**
     * 게임 시작하면, 각 유저의 개인 포인트를 db에서 가져와서
     * redis 방정보 초기화
     */
    public void startGame(Long roomId, Long user1Id, Long user2Id, int totalRounds) {
        // 1. DB에서 유저 포인트 조회
        Long point1 = userRepository.findPointByUserId(user1Id);
        Long point2 = userRepository.findPointByUserId(user2Id);

        // 2. Redis에 게임 상태 초기화
        gameRedisService.initGame(roomId, totalRounds, user1Id, point1, user2Id, point2);

        // 3. WebSocket으로 GAME_START 메시지 전송은 별도 처리

    }
}
