package S13P11A708.backend.controller;

import S13P11A708.backend.dto.request.gameRoom.CreateGameRoomRequestDto;
import S13P11A708.backend.dto.response.gameRoom.GameRoomResponseDto;
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

    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinRoom(@PathVariable Long roomId,
                                      @AuthenticationPrincipal CustomOAuth2User oAuth2User){
        gameRoomService.joinRoom(roomId, oAuth2User.getUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable Long roomId,
                                       @AuthenticationPrincipal CustomOAuth2User oAuth2User){
        gameRoomService.leaveRoom(roomId, oAuth2User.getUserId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{roomId}/ready")
    public ResponseEntity<?> toggleReady(@PathVariable Long roomId,
                                         @AuthenticationPrincipal CustomOAuth2User oAuth2User){
        gameRoomService.toggleReady(roomId, oAuth2User.getUserId());
        return ResponseEntity.ok().build();
    }
}
