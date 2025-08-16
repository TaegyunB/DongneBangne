
package S13P11A708.backend.config;

import S13P11A708.backend.websocket.SignalingHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class SignalingWebSocketConfig implements WebSocketConfigurer {
    // signal로 요청이 들어오면 signalingHandler가 동작한다.
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){
        // /signal/{roomId} 로 접속
        registry.addHandler(signalingSocketHandler(), "/signal/**")
                .setAllowedOrigins("*");
    }

    @Bean
    public org.springframework.web.socket.WebSocketHandler signalingSocketHandler() {
        return new SignalingHandler();
    }
}
