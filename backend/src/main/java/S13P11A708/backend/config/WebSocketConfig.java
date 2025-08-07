package S13P11A708.backend.config;

import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * STOMP 엔드포인트 등록
     * 클라이언트가 WebSocket 연결을 맺기 위해 사용할 엔드포인트 url 설정
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-game")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * 메시지를 클라이언트에게 브로드캐스트할 때 사용할 주제 정의
     * 클라이언트에서 서버로 메세지 보낼 때 사용할 목적지 접두사 설정
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // subscribe destination(서버 -> 클라이언트)
        registry.enableSimpleBroker("/sub", "/queue");
        // send destination(클라이언트 -> 서버)
        registry.setApplicationDestinationPrefixes("/pub");


    }


}
