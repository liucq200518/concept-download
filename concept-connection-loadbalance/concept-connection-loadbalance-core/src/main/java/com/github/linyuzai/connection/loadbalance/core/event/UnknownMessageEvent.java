package com.github.linyuzai.connection.loadbalance.core.event;

import com.github.linyuzai.connection.loadbalance.core.concept.Connection;
import com.github.linyuzai.connection.loadbalance.core.concept.ConnectionLoadBalanceConcept;
import com.github.linyuzai.connection.loadbalance.core.concept.IdConnection;
import lombok.Getter;

@Getter
public class UnknownMessageEvent implements ConnectionEvent {

    private final Connection connection;

    private final Object message;

    public UnknownMessageEvent(Object id, String type, Object message, ConnectionLoadBalanceConcept concept) {
        this.connection = new IdConnection(id, type, concept);
        this.message = message;
    }
}
