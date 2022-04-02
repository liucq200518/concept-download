package com.github.linyuzai.plugin.core.match;

import com.github.linyuzai.plugin.core.context.PluginContext;
import com.github.linyuzai.plugin.core.filter.NameFilter;
import com.github.linyuzai.plugin.core.filter.PathFilter;
import lombok.Getter;
import lombok.NonNull;

import java.lang.annotation.Annotation;

/**
 * {@link PluginMatcher} 抽象类。
 * 支持通过路径 {@link PathFilter} 和名称 {@link NameFilter} 过滤匹配
 *
 * @param <T> 插件类型
 */
@Getter
public abstract class AbstractPluginMatcher<T> implements PluginMatcher {

    /**
     * 路径逻辑器
     */
    private PathFilter pathFilter;

    /**
     * 名称过滤器
     */
    private NameFilter nameFilter;

    public AbstractPluginMatcher(@NonNull Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == PluginPath.class) {
                String[] packages = ((PluginPath) annotation).value();
                if (packages.length > 0) {
                    pathFilter = new PathFilter(packages);
                }
            } else if (annotation.annotationType() == PluginName.class) {
                String[] classNames = ((PluginName) annotation).value();
                if (classNames.length > 0) {
                    nameFilter = new NameFilter(classNames);
                }
            }
        }
    }

    @Override
    public Object match(PluginContext context) {
        T source = context.get(getKey());
        T filter = filter(source);
        if (isEmpty(filter)) {
            return null;
        }
        return filter;
    }

    /**
     * 结合路径和名称进行过滤
     *
     * @param pathAndName 路径名称
     * @return 是否满足过滤条件
     */
    public boolean filterWithAnnotation(String pathAndName) {
        if (pathFilter != null && !pathFilter.matchPath(pathAndName)) {
            return false;
        }
        if (nameFilter != null && !nameFilter.matchName(pathAndName)) {
            return false;
        }
        return true;
    }

    /**
     * 用于提取上下文中的插件
     *
     * @return 插件在上下文中的 key
     */
    public abstract Object getKey();

    /**
     * 基于泛型的插件过滤匹配
     *
     * @param source 被过滤的插件
     * @return 过滤后的插件
     */
    public abstract T filter(T source);

    /**
     * 过滤后的插件是否为空
     *
     * @param filter 过滤后的插件
     * @return 如果为空返回 true 否则返回 false
     */
    public abstract boolean isEmpty(T filter);
}
