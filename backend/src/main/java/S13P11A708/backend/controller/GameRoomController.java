package S13P11A708.backend.controller;

import S13P11A708.backend.dto.request.gameRoom.CreateGameRoomRequestDto;
import S13P11A708.backend.dto.response.gameRoom.GameRoomResponseDto;
import S13P11A708.backend.dto.response.gameRoomUser.CurrentRoomUserResponseDto;
import S13P11A708.backend.dto.response.gameRoomUser.GameRoomUserResponseDto;
import S13P11A708.backend.dto.response.gameRoomUser.ReadyGameRoomUserResponseDto;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/game-rooms")
public class GameRoomController {

    private final GameRoomService gameRoomService;
 
    /**
     * 게임방 생성
     */
    @PostMapping
    public ResponseEntity<GameRoomResponseDto> createRoom(@RequestBody CreateGameRoomRequestDto request,
                                                          @AuthenticationPrincipal CustomOAuth2User user){
        return ResponseEntity.ok(gameRoomService.createRoom(request, user.getUserId()));
    }

    /**
     * 들어갈 수 있는 방(WAITING 상태) 조회
     */
    @GetMapping
    public ResponseEntity<List<GameRoomResponseDto>> getRooms(){
        return ResponseEntity.ok(gameRoomService.getWaitingRooms());
    }


    /**
     * 사용자의 게임방 참여
     * 나와 같은 경로당 사람과는 게임방 참여 못함
     * 최대 인원 2명
     */
    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinRoom(@PathVariable Long roomId,
                                      @AuthenticationPrincipal CustomOAuth2User oAuth2User){
        GameRoomUserResponseDto response = gameRoomService.joinRoom(roomId, oAuth2User.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자의 게임방 나가기
     */
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable Long roomId,
                                       @AuthenticationPrincipal CustomOAuth2User oAuth2User){
        gameRoomService.leaveRoom(roomId, oAuth2User.getUserId());
        return ResponseEntity.ok("해당 게임방을 나갑니다.");
    }

    /**
     * 참여한 게임방에서 사용자가 준비상태 전환하는 기능
     * response에서 두 참가자 id 모두 제공
     */
    @PutMapping("/{roomId}/ready")
    public ResponseEntity<?> toggleReady(@PathVariable Long roomId,
                                         @AuthenticationPrincipal CustomOAuth2User oAuth2User){
        ReadyGameRoomUserResponseDto response = gameRoomService.toggleReady(roomId, oAuth2User.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 게임방의 참가자 목록 조회 (polling 필요)
     */
    @GetMapping("/{roomId}/waiting-users")
    public ResponseEntity<List<CurrentRoomUserResponseDto>> getWaitingUsers(@PathVariable Long roomId){
        List<CurrentRoomUserResponseDto> response = gameRoomService.getWaitingUsers(roomId);
        return ResponseEntity.ok(response);
    }
}
