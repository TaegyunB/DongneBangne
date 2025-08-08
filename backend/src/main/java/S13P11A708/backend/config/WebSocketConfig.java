package S13P11A708.backend.config;

import S13P11A708.backend.websocket.SignalingHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-game")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // subscribe destination(서버 -> 클라이언트)
        registry.enableSimpleBroker("/sub", "/queue");
        // send destination(클라이언트 -> 서버)
        registry.setApplicationDestinationPrefixes("/pub");
    }
}
