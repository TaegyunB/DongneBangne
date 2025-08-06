package S13P11A708.backend.websocket;

import S13P11A708.backend.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameSocketService {

    private final GameBroadcaster broadcaster;
    private final GameService gameService;

//    public void routeMessage(GameSocketMessage message) {
//        switch (message.getType()) {
//
//        }
//    }


}
