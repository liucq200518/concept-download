package com.github.linyuzai.plugin.core.filter;

import com.github.linyuzai.plugin.core.context.PluginContext;
import com.github.linyuzai.plugin.core.exception.PluginException;
import com.github.linyuzai.plugin.core.resolver.PluginResolver;

public interface PluginFilter {

    void filter(PluginContext context);

    default Class<? extends PluginResolver> filterWith() {
        Class<?> clazz = getClass();
        while (clazz != null) {
            FilterWithResolver annotation = clazz.getAnnotation(FilterWithResolver.class);
            if (annotation != null) {
                return annotation.value();
            }
            clazz = clazz.getSuperclass();
        }
        throw new PluginException("A plugin resolver must be bound");
    }
}
