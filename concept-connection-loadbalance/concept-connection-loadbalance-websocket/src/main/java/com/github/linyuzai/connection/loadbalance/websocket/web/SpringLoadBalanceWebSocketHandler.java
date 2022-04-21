package com.github.linyuzai.connection.loadbalance.websocket.web;

import com.github.linyuzai.connection.loadbalance.websocket.concept.WebSocketLoadBalanceConcept;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.web.socket.*;

@AllArgsConstructor
public class SpringLoadBalanceWebSocketHandler implements WebSocketHandler {

    private final WebSocketLoadBalanceConcept concept;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        concept.add(session, null);
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) throws Exception {
        if (message instanceof PingMessage) {
            concept.message(session.getId(), ((PingMessage) message).getPayload().array());
        } else if (message instanceof PongMessage) {
            concept.message(session.getId(), ((PongMessage) message).getPayload().array());
        } else if (message instanceof BinaryMessage) {
            concept.message(session.getId(), ((BinaryMessage) message).getPayload().array());
        } else if (message instanceof TextMessage) {
            concept.message(session.getId(), ((TextMessage) message).asBytes());
        }
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {

    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
