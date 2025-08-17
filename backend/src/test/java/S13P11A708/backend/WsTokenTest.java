//package S13P11A708.backend;
//
//import S13P11A708.backend.domain.enums.GameMessageType;
//import S13P11A708.backend.dto.webSocket.GameAnsRequestMessage;
//import S13P11A708.backend.service.GameService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.Timeout;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.http.HttpHeaders;
//import org.springframework.messaging.converter.MappingJackson2MessageConverter;
//import org.springframework.messaging.simp.stomp.StompHeaders;
//import org.springframework.messaging.simp.stomp.StompSession;
//import org.springframework.messaging.simp.stomp.StompSessionHandler;
//import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
//import org.springframework.web.socket.WebSocketHttpHeaders;
//import org.springframework.web.socket.client.standard.StandardWebSocketClient;
//import org.springframework.web.socket.messaging.WebSocketStompClient;
//
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class WsTokenTest {
//    @LocalServerPort
//    int port;
//
//    @Autowired
//    GameService gameService; // 실제 빈 (목킹 안 함)으로 호출 확인할 수도 있음
//
//    @Test
//    @Timeout(20)
//    void websocket_cookie_token_authentication() throws Exception {
//        // ✅ 여기에 현재 유효한 로컬 서버 토큰 입력
//        String token = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInJvbGUiOiJST0xFX0dVRVNUIiwiaWF0IjoxNzU1MTM0ODA0LCJleHAiOjE3NTUyMjEyMDR9.QoVx45QFlBrK9Rf-rZOH4ofOZDLR42JgMQsh9eEUc-0";
//
//        // 1) WebSocketStompClient 준비
//        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
//        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
//
//        // 2) Cookie 헤더에 토큰 넣기
//        WebSocketHttpHeaders wsHeaders = new WebSocketHttpHeaders();
//        wsHeaders.add(HttpHeaders.COOKIE, "access_token=" + token);
//
//        StompHeaders connectHeaders = new StompHeaders();
//
//        CountDownLatch connectLatch = new CountDownLatch(1);
//
//        final StompSession[] sessionHolder = new StompSession[1];
//
//        StompSessionHandler handler = new StompSessionHandlerAdapter() {
//            @Override
//            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
//                System.out.println("✅ 연결 성공");
//                sessionHolder[0] = session;
//                connectLatch.countDown();
//            }
//
//            @Override
//            public void handleTransportError(StompSession session, Throwable exception) {
//                exception.printStackTrace();
//            }
//        };
//
//        String url = "ws://localhost:" + port + "/ws-game";
//        stompClient.connect(url, wsHeaders, connectHeaders, handler);
//
//        // 3) 연결 대기
//        if (!connectLatch.await(5, TimeUnit.SECONDS)) {
//            throw new AssertionError("❌ STOMP CONNECT 실패");
//        }
//
//        // 4) 인증된 상태에서 서버로 메시지 보내기
//        long roomId = 1L;
//        String answer = "고향역";
//        var msg = new GameAnsRequestMessage(GameMessageType.ANSWER_SUBMIT, roomId, answer);
//
//        sessionHolder[0].send("/pub/answer", msg);
//
//        // 5) 여기서는 로그나 서버 코드에서 Principal 확인
//        //    -> Principal.getName() 또는 CustomOAuth2User 안의 userId가 토큰 정보와 일치하는지 확인
//        Thread.sleep(1000); // 서버 처리 기다림
//    }
//}
