package com.github.linyuzai.event.core.concept;

import com.github.linyuzai.event.core.codec.EventDecoder;
import com.github.linyuzai.event.core.codec.EventEncoder;
import com.github.linyuzai.event.core.context.EventContext;
import com.github.linyuzai.event.core.context.EventContextFactory;
import com.github.linyuzai.event.core.endpoint.EventEndpoint;
import com.github.linyuzai.event.core.error.EventErrorHandler;
import com.github.linyuzai.event.core.lifecycle.EventConceptLifecycleListener;
import com.github.linyuzai.event.core.publisher.EventPublisher;
import com.github.linyuzai.event.core.engine.EventEngine;
import com.github.linyuzai.event.core.exchange.EventExchange;
import com.github.linyuzai.event.core.subscriber.EventSubscriber;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 事件概念实现
 */
@Getter
@Setter
public class EventConceptImpl implements EventConcept {

    /**
     * 事件引擎缓存
     */
    protected final Map<String, EventEngine> engineMap = new ConcurrentHashMap<>();

    /**
     * 生命周期监听器缓存
     */
    protected final List<EventConceptLifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<>();

    /**
     * 事件上下文工厂
     */
    private EventContextFactory contextFactory;

    /**
     * 默认事件交换机
     */
    private EventExchange exchange;

    /**
     * 默认事件编码器
     */
    private EventEncoder encoder;

    /**
     * 默认事件解码器
     */
    private EventDecoder decoder;

    /**
     * 默认异常处理器
     */
    private EventErrorHandler errorHandler;

    @Override
    public void initialize() {
        for (EventConceptLifecycleListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.onInitialize(this);
        }
    }

    @Override
    public void destroy() {
        for (EventConceptLifecycleListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.onDestroy(this);
        }
    }

    @Override
    public EventTemplate template() {
        return new EventTemplateImpl();
    }

    /**
     * 发布事件
     *
     * @param event   事件
     * @param context 事件上下文
     */
    protected void publishWithContext(Object event, EventContext context) {
        EventExchange exchange = applyExchange(context);
        EventPublisher publisher = context.get(EventPublisher.class);
        Collection<EventEndpoint> endpoints = exchange.exchange(this);
        for (EventEndpoint endpoint : endpoints) {
            EventContext prepare = prepareContext(context, endpoint);
            prepare.put(EventPublisher.class, usePublisher(endpoint, publisher));
            endpoint.publish(event, prepare);
        }
    }

    /**
     * 订阅事件
     *
     * @param consumer 事件消费者
     * @param context  事件上下文
     */
    protected void subscribeWithContext(Consumer<Object> consumer, EventContext context) {
        EventExchange exchange = applyExchange(context);
        EventSubscriber subscriber = context.get(EventSubscriber.class);
        Collection<EventEndpoint> endpoints = exchange.exchange(this);
        for (EventEndpoint endpoint : endpoints) {
            EventContext prepare = prepareContext(context, endpoint);
            prepare.put(EventSubscriber.class, useSubscriber(endpoint, subscriber));
            endpoint.subscribe(consumer, prepare);
        }
    }

    /**
     * 确定事件交换机
     */
    protected EventExchange applyExchange(EventContext context) {
        EventExchange exchange = context.get(EventExchange.class);
        EventExchange exchangeToUse = useExchange(exchange);
        context.put(EventExchange.class, exchangeToUse);
        return exchangeToUse;
    }

    /**
     * 准备事件上下文
     * <p>
     * 进行复制，基于对应的端点设置事件上下文
     */
    protected EventContext prepareContext(EventContext context, EventEndpoint endpoint) {
        EventContext duplicate = context.duplicate();
        duplicate.put(EventConcept.class, this);
        EventEncoder encoder = duplicate.get(EventEncoder.class);
        duplicate.put(EventEncoder.class, useEncoder(endpoint, encoder));
        EventDecoder decoder = duplicate.get(EventDecoder.class);
        duplicate.put(EventDecoder.class, useDecoder(endpoint, decoder));
        EventErrorHandler errorHandler = duplicate.get(EventErrorHandler.class);
        duplicate.put(EventErrorHandler.class, useErrorHandler(endpoint, errorHandler));
        return duplicate;
    }

    @Override
    public EventEngine getEngine(String name) {
        return engineMap.get(name);
    }

    @Override
    public Collection<EventEngine> getEngines() {
        return Collections.unmodifiableCollection(engineMap.values());
    }

    @Override
    public void addEngines(Collection<? extends EventEngine> engines) {
        for (EventEngine engine : engines) {
            this.engineMap.put(engine.getName(), engine);
        }
    }

    @Override
    public void removeEngines(Collection<String> engines) {
        for (String engine : engines) {
            this.engineMap.remove(engine);
        }
    }

    @Override
    public void addLifecycleListeners(Collection<? extends EventConceptLifecycleListener> lifecycleListeners) {
        this.lifecycleListeners.addAll(lifecycleListeners);
    }

    @Override
    public void removeLifecycleListeners(Collection<? extends EventConceptLifecycleListener> lifecycleListeners) {
        this.lifecycleListeners.removeAll(lifecycleListeners);
    }

    /**
     * 选择事件交换机
     * <p>
     * 如果指定了事件交换机则使用指定的事件交换机
     * <p>
     * 如果未指定则使用默认事件交换机
     * <p>
     * 如果默认事件交换机为 null 则发布事件到所有端点
     */
    protected EventExchange useExchange(EventExchange exchange) {
        if (exchange != null) {
            return exchange;
        }
        if (this.exchange != null) {
            return this.exchange;
        }
        return EventExchange.ALL;
    }

    /**
     * 选择事件编码器
     * <p>
     * 如果指定了事件编码器则使用指定的事件编码器
     * <p>
     * 如果未指定事件编码器则使用事件端点的事件编码器
     * <p>
     * 如果事件端点的事件编码器为 null 则使用事件引擎的事件编码器
     * <p>
     * 如果事件引擎的事件编码器为 null 则使用默认事件编码器
     */
    protected EventEncoder useEncoder(EventEndpoint endpoint, EventEncoder encoder) {
        if (encoder != null) {
            return encoder;
        }
        if (endpoint.getEncoder() != null) {
            return endpoint.getEncoder();
        }
        if (endpoint.getEngine().getEncoder() != null) {
            return endpoint.getEngine().getEncoder();
        }
        return this.encoder;
    }

    /**
     * 选择事件解码器
     * <p>
     * 如果指定了事件解码器则使用指定的事件解码器
     * <p>
     * 如果未指定事件解码器则使用事件端点的事件解码器
     * <p>
     * 如果事件端点的事件解码器为 null 则使用事件引擎的事件解码器
     * <p>
     * 如果事件引擎的事件解码器为 null 则使用默认事件解码器
     */
    protected EventDecoder useDecoder(EventEndpoint endpoint, EventDecoder decoder) {
        if (decoder != null) {
            return decoder;
        }
        if (endpoint.getDecoder() != null) {
            return endpoint.getDecoder();
        }
        if (endpoint.getEngine().getDecoder() != null) {
            return endpoint.getEngine().getDecoder();
        }
        return this.decoder;
    }

    /**
     * 选择异常处理器
     * <p>
     * 如果指定了异常处理器则使用指定的异常处理器
     * <p>
     * 如果未指定异常处理器则使用事件端点的异常处理器
     * <p>
     * 如果事件端点的异常处理器为 null 则使用事件引擎的异常处理器
     * <p>
     * 如果事件引擎的异常处理器为 null 则使用默认异常处理器
     */
    protected EventErrorHandler useErrorHandler(EventEndpoint endpoint, EventErrorHandler errorHandler) {
        if (errorHandler != null) {
            return errorHandler;
        }
        if (endpoint.getErrorHandler() != null) {
            return endpoint.getErrorHandler();
        }
        if (endpoint.getEngine().getErrorHandler() != null) {
            return endpoint.getEngine().getErrorHandler();
        }
        return this.errorHandler;
    }

    /**
     * 选择事件发布器
     * <p>
     * 如果指定了事件发布器则使用指定的事件发布器
     * <p>
     * 如果未指定事件发布器则使用事件端点的事件发布器
     * <p>
     * 如果事件端点的事件发布器为 null 则使用事件引擎的事件发布器
     * <p>
     * 如果事件引擎的事件发布器为 null 则返回 null
     */
    protected EventPublisher usePublisher(EventEndpoint endpoint, EventPublisher publisher) {
        if (publisher != null) {
            return publisher;
        }
        if (endpoint.getPublisher() != null) {
            return endpoint.getPublisher();
        }
        if (endpoint.getEngine().getPublisher() != null) {
            return endpoint.getEngine().getPublisher();
        }
        return null;
    }

    /**
     * 选择事件订阅器
     * <p>
     * 如果指定了事件订阅器则使用指定的事件订阅器
     * <p>
     * 如果未指定事件订阅器则使用事件端点的事件订阅器
     * <p>
     * 如果事件端点的事件订阅器为 null 则使用事件引擎的事件订阅器
     * <p>
     * 如果事件引擎的事件订阅器为 null 则返回 null
     */
    protected EventSubscriber useSubscriber(EventEndpoint endpoint, EventSubscriber subscriber) {
        if (subscriber != null) {
            return subscriber;
        }
        if (endpoint.getSubscriber() != null) {
            return endpoint.getSubscriber();
        }
        if (endpoint.getEngine().getSubscriber() != null) {
            return endpoint.getEngine().getSubscriber();
        }
        return null;
    }

    /**
     * 事件操作者的实现
     */
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    protected class EventTemplateImpl implements EventTemplate {

        /**
         * 上下文缓存
         */
        protected EventContext context = contextFactory.create();

        @Override
        public EventTemplate exchange(EventExchange exchange) {
            context.put(EventExchange.class, exchange);
            return this;
        }

        @Override
        public EventTemplate encoder(EventEncoder encoder) {
            context.put(EventEncoder.class, encoder);
            return this;
        }

        @Override
        public EventTemplate decoder(EventDecoder decoder) {
            context.put(EventDecoder.class, decoder);
            return this;
        }

        @Override
        public EventTemplate publisher(EventPublisher publisher) {
            context.put(EventPublisher.class, publisher);
            return this;
        }

        @Override
        public EventTemplate subscriber(EventSubscriber subscriber) {
            context.put(EventSubscriber.class, subscriber);
            return this;
        }

        @Override
        public EventTemplate error(Consumer<Throwable> errorHandler) {
            return error((e, endpoint, context) -> errorHandler.accept(e));
        }

        @Override
        public EventTemplate error(BiConsumer<Throwable, EventEndpoint> errorHandler) {
            return error((e, endpoint, context) -> errorHandler.accept(e, endpoint));
        }

        @Override
        public EventTemplate error(EventErrorHandler errorHandler) {
            context.put(EventErrorHandler.class, errorHandler);
            return this;
        }

        @Override
        public EventTemplate context(Object key, Object value) {
            context.put(key, value);
            return this;
        }

        @Override
        public void publish(Object event) {
            publishWithContext(event, context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void subscribe(Consumer<?> consumer) {
            subscribeWithContext((Consumer<Object>) consumer, context);
        }
    }
}
