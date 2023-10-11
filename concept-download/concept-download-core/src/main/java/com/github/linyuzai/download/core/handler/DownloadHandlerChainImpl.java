package com.github.linyuzai.download.core.handler;

import com.github.linyuzai.download.core.context.DownloadContext;
import com.github.linyuzai.reactive.core.concept.ReactiveConcept;
import com.github.linyuzai.reactive.core.concept.ReactiveObject;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Supplier;

/**
 * {@link DownloadHandlerChain} 的默认实现。
 */
@AllArgsConstructor
public class DownloadHandlerChainImpl implements DownloadHandlerChain {

    /**
     * 下标，指定处理器位置
     */
    private int index;

    /**
     * 处理器列表
     */
    private final List<DownloadHandler> handlers;

    /**
     * 如果可以获得下一个处理器则调用，否则返回 {@link Mono#empty()}。
     *
     * @param context {@link DownloadContext}
     */
    @Override
    public ReactiveObject<Void> next(DownloadContext context) {
        ReactiveConcept reactive = context.get(ReactiveConcept.class);
        return reactive.objectFactory().defer(() -> {
            if (index < handlers.size()) {
                DownloadHandler handler = handlers.get(index);
                DownloadHandlerChain chain = new DownloadHandlerChainImpl(index + 1, handlers);
                return handler.handle(context, chain);
            } else {
                return reactive.objectFactory().empty();
            }
        });
    }
}
