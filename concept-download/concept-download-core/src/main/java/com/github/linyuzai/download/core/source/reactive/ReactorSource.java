package com.github.linyuzai.download.core.source.reactive;

import com.github.linyuzai.download.core.context.DownloadContext;
import com.github.linyuzai.download.core.executor.DownloadExecutor;
import com.github.linyuzai.download.core.source.Source;
import lombok.SneakyThrows;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;
import java.util.concurrent.Executor;

public interface ReactorSource extends Source {

    Mono<Void> preload(DownloadContext context);

    @SneakyThrows
    static void preload(DownloadContext context, Collection<? extends ReactorSource> sources) {
        Executor executor = DownloadExecutor.getExecutor(context);
        Flux.fromIterable(sources)
                .parallel()
                .runOn(executor == null ? Schedulers.immediate() : Schedulers.fromExecutor(executor))
                .flatMap(it -> it.preload(context))
                //.runOn(Schedulers.immediate())
                .sequential()
                .collectList()
                .toFuture()
                .get();
    }
}
