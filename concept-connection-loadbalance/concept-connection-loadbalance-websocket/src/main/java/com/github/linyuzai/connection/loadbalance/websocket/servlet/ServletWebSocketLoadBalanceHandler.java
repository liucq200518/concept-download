package com.github.linyuzai.connection.loadbalance.websocket.servlet;

import com.github.linyuzai.connection.loadbalance.core.concept.Connection;
import com.github.linyuzai.connection.loadbalance.core.message.MessageCodecAdapter;
import com.github.linyuzai.connection.loadbalance.websocket.concept.WebSocketLoadBalanceConcept;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.web.socket.*;

@AllArgsConstructor
public class ServletWebSocketLoadBalanceHandler implements WebSocketHandler {

    protected final WebSocketLoadBalanceConcept concept;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        ServletWebSocketConnection connection =
                new ServletWebSocketConnection(session, Connection.Type.OBSERVABLE);
        MessageCodecAdapter adapter = concept.getMessageCodecAdapter();
        connection.setMessageEncoder(adapter.getMessageEncoder(Connection.Type.OBSERVABLE));
        connection.setMessageDecoder(adapter.getMessageDecoder(Connection.Type.OBSERVABLE));
        connection.setConcept(concept);
        concept.open(connection);
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) throws Exception {
        concept.message(session.getId(), Connection.Type.OBSERVABLE, message);
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
        concept.error(session.getId(), Connection.Type.OBSERVABLE, exception);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        concept.close(session.getId(), Connection.Type.OBSERVABLE, closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
