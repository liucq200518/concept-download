package com.github.linyuzai.connection.loadbalance.core.event;

import com.github.linyuzai.connection.loadbalance.core.concept.ConnectionLoadBalanceConcept;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;

/**
 * 事件发布者。
 * <p>
 * Event publisher.
 */
public interface ConnectionEventPublisher {

    /**
     * 发布事件。
     * <p>
     * Publish event.
     */
    void publish(Object event, ConnectionLoadBalanceConcept concept);

    default void publish(Object event) {
        publish(event, null);
    }

    /**
     * 注册事件监听器。
     * <p>
     * Register event listener.
     */
    void register(ConnectionEventListener listener, ConnectionLoadBalanceConcept concept);

    default void register(ConnectionEventListener listener) {
        register(listener, null);
    }

    /**
     * 注册事件监听器。
     * <p>
     * Register event listeners.
     */
    void register(Collection<? extends ConnectionEventListener> listeners, ConnectionLoadBalanceConcept concept);

    default void register(Collection<? extends ConnectionEventListener> listeners) {
        register(listeners, null);
    }

    /**
     * 事件发布者代理。
     * <p>
     * Delegate of event publisher.
     */
    @Getter
    @RequiredArgsConstructor
    class Delegate implements ConnectionEventPublisher {

        private final ConnectionLoadBalanceConcept concept;

        private final ConnectionEventPublisher delegate;

        public static ConnectionEventPublisher delegate(ConnectionLoadBalanceConcept concept,
                                                        ConnectionEventPublisher delegate) {
            return new Delegate(concept, delegate);
        }

        @Override
        public void publish(Object event, ConnectionLoadBalanceConcept concept) {
            delegate.publish(event, concept);
        }

        @Override
        public void publish(Object event) {
            delegate.publish(event, concept);
        }

        @Override
        public void register(ConnectionEventListener listener, ConnectionLoadBalanceConcept concept) {
            delegate.register(listener, concept);
        }

        @Override
        public void register(ConnectionEventListener listener) {
            delegate.register(listener, concept);
        }

        @Override
        public void register(Collection<? extends ConnectionEventListener> listeners, ConnectionLoadBalanceConcept concept) {
            delegate.register(listeners, concept);
        }

        @Override
        public void register(Collection<? extends ConnectionEventListener> listeners) {
            delegate.register(listeners, concept);
        }
    }
}
