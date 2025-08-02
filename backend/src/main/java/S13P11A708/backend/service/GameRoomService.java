package S13P11A708.backend.service;

import S13P11A708.backend.domain.GameRoom;
import S13P11A708.backend.domain.GameRoomUser;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.domain.enums.GameStatus;
import S13P11A708.backend.dto.request.gameRoom.CreateGameRoomRequestDto;
import S13P11A708.backend.dto.response.gameRoom.GameRoomResponseDto;
import S13P11A708.backend.repository.GameRoomRepository;
import S13P11A708.backend.repository.GameRoomUserRepository;
import S13P11A708.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameRoomService {
    private final GameRoomRepository gameRoomRepository;
    private final GameRoomUserRepository gameRoomUserRepository;
    private final UserRepository userRepository;

    /**
     * 게임방 생성
     */
    @Transactional
    public GameRoomResponseDto createRoom(CreateGameRoomRequestDto request,  Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("사용자 정보 없음"));

        GameRoom room = GameRoom.builder()
                .roomTitle(request.getRoomTitle())
                .gameRound(request.getGameRound())
                .musicEra(request.getMusicEra())
                .category(request.getCategory())
                .gameStatus(GameStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build();

        gameRoomRepository.save(room);

        return GameRoomResponseDto.from(room);
    }

    /**
     * WAITING 상태인 게임방 조회
     */
    public List<GameRoomResponseDto> getWaitingRooms(){
        return gameRoomRepository.findByGameStatus(GameStatus.WAITING)
                .stream()
                .map(GameRoomResponseDto::from)
                .toList();
    }
}
