package S13P11A708.backend.service;

import S13P11A708.backend.domain.GameRoom;
import S13P11A708.backend.domain.GameRoomUser;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.domain.enums.GameStatus;
import S13P11A708.backend.dto.request.gameRoom.CreateGameRoomRequestDto;
import S13P11A708.backend.dto.request.gameRoom.JoinGameRoomRequestDto;
import S13P11A708.backend.dto.response.gameRoom.GameRoomResponseDto;
import S13P11A708.backend.dto.response.gameRoomUser.GameRoomUserResponseDto;
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

    /**
     * 유저가 특정 게임방에 입장
     * 참가자 수가 2명이 넘지 않도록 제한
     * 같은 경로당 회원끼리는 게임하지 않도록 제한
     */
    @Transactional
    public GameRoomUserResponseDto joinRoom(Long roomId, Long userId){
        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("방이 존재하지 않습니다."));

        if(room.getGameStatus() != GameStatus.WAITING){
            throw new RuntimeException("대기 중인 방만 입장할 수 있습니다. 다른 방을 찾아주세요.");
        }

        //room.getParticipants().size()
        if(gameRoomUserRepository.countByGameRoomId_Id(roomId) >= 2){
            throw new RuntimeException("방 정원이 찼습니다.");
        }

        //참여 요청하는 유저
        User joinUser = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("유저 정보가 없습니다."));

        //기존에 방에 참여하고 있던 유저
        List<GameRoomUser> exisitingParticipants = gameRoomUserRepository.findAllByGameRoomId_Id(roomId);

        for(GameRoomUser participant : exisitingParticipants){
            User existingUser = participant.getUserId();

            if(existingUser.getSeniorCenter().getId()
                    .equals(joinUser.getSeniorCenter().getId())){
                throw new RuntimeException("같은 경로당 소속 회원끼리는 함께 게임에 참여할 수 없습니다.");
            }
        }

        //참가자 등록
        GameRoomUser participant = GameRoomUser.builder()
                .gameRoomId(room)
                .userId(joinUser)
                .ready(false)
                .build();

        gameRoomUserRepository.save(participant);

        return GameRoomUserResponseDto.builder()
                .gameRoomId(room.getId())
                .userId(joinUser.getId())
                .ready(false)
                .message(joinUser.getNickname()+"님이 "+room.getRoomTitle()+" 방에 참여합니다.")
                .build();
    }

    /**
     * 유저 게임방 나가기
     * 유저와 게임방 정보 확인 후 -> 삭제
     */
    @Transactional
    public void leaveRoom(Long roomId, Long userId){
        gameRoomUserRepository.deleteByGameRoomId_IdAndUserId_Id(roomId, userId);
    }

    /**
     * 유저의 게임 준비 버튼 true/false
     */
    @Transactional
    public void toggleReady(Long roomId, Long userId){
        GameRoomUser gameRoomUser = gameRoomUserRepository.findByGameRoomId_IdAndUserId_Id(roomId, userId)
                .orElseThrow(() -> new RuntimeException("해당 방 참가자가 아닙니다."));
        //준비 상태 변경 false->true
        gameRoomUser.userGetReady();

        List<GameRoomUser> participants = gameRoomUserRepository.findAllByGameRoomId_Id(roomId);

        //참가자가 2명이고 모두 ready 상태인 경우
        boolean allReady = participants.size() == 2 && participants.stream()
                .allMatch(GameRoomUser::isReady);

        if(allReady){
            GameRoom room = gameRoomUser.getGameRoomId();
            room.changeGameStatus(GameStatus.PROGRESS); //방 상태를 게임 진행 상태로 변경
        }
    }
}
