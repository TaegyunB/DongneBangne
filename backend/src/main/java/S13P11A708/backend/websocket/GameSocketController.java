package S13P11A708.backend.websocket;

import S13P11A708.backend.websocket.GameSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameSocketController {

    private GameSocketService gameSocketService;

//    @MessageMapping("/game/send")
//    public void handleMessage(GameSocketMessage message) {
//        gameSocketService.routeMessage(message);
//    }
}
