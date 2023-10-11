package com.github.linyuzai.download.core.load;

import com.github.linyuzai.download.core.context.DownloadContext;
import com.github.linyuzai.download.core.source.Source;
import com.github.linyuzai.download.core.source.multiple.MultipleSource;
import com.github.linyuzai.reactive.core.concept.ReactiveConcept;
import com.github.linyuzai.reactive.core.concept.ReactiveObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 支持并发的 {@link SourceLoader}。
 */
@NoArgsConstructor
@AllArgsConstructor
public abstract class ConcurrentSourceLoader implements SourceLoader {

    /**
     * 单个文件的时候串行加载
     */
    @Getter
    @Setter
    private boolean serialOnSingle = true;

    /**
     * 将 {@link Source} 中需要异步加载的部分进行并发加载，
     * 并与同步加载之后的 {@link Source} 合并。
     *
     * @param source  {@link Source}
     * @param context {@link DownloadContext}
     * @return 加载后的 {@link Source}
     */
    @Override
    public ReactiveObject<Source> load(Source source, DownloadContext context) {
        Collection<Source> syncSources = new ArrayList<>();
        Collection<Source> asyncSources = new ArrayList<>();
        Collection<Source> sources = source.list();
        for (Source s : sources) {
            if (s.isAsyncLoad()) {
                asyncSources.add(s);
            } else {
                syncSources.add(s);
            }
        }

        ReactiveConcept reactive = context.get(ReactiveConcept.class);
        if (asyncSources.isEmpty()) {
            return reactive.collectionFactory()
                    .fromIterable(syncSources)
                    .flatMap(it -> it.load(context))
                    .collectList()
                    .map(MultipleSource::new);
        } else {
            if (asyncSources.size() == 1 && serialOnSingle) {
                syncSources.add(asyncSources.iterator().next());
                return reactive.collectionFactory()
                        .fromIterable(syncSources)
                        .flatMap(it -> it.load(context))
                        .collectList()
                        .map(MultipleSource::new);
            } else {
                ReactiveObject<Source> syncObject = reactive.collectionFactory()
                        .fromIterable(syncSources)
                        .flatMap(it -> it.load(context))
                        .collectList()
                        .map(MultipleSource::new);
                ReactiveObject<Source> asyncObject = concurrentLoad(asyncSources, context);
                return reactive.objectFactory().zip(syncObject, asyncObject, (sync, async) -> {
                    Collection<Source> newSources = new ArrayList<>();
                    newSources.addAll(sync.list());
                    newSources.addAll(async.list());
                    return new MultipleSource(newSources);
                });
            }
        }
    }


    /**
     * 并发加载。
     *
     * @param sources {@link Source} 集合
     * @param context {@link DownloadContext}
     * @return 加载后的 {@link Source}
     */
    public abstract ReactiveObject<Source> concurrentLoad(Collection<Source> sources, DownloadContext context);
}
