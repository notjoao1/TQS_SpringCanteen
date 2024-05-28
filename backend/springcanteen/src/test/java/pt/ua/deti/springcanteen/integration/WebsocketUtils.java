package pt.ua.deti.springcanteen.integration;

import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WebsocketUtils {

    public static StompSession connectAsyncWithHeaders(String websocketURL, WebSocketStompClient webSocketStompClient, StompHeaders userHandshakeHeaders) throws ExecutionException, InterruptedException, TimeoutException {
        return webSocketStompClient.connectAsync(
                websocketURL, new WebSocketHttpHeaders(), userHandshakeHeaders, new StompSessionHandlerAdapter() {}
        ).get(1, TimeUnit.SECONDS);
    }

}
