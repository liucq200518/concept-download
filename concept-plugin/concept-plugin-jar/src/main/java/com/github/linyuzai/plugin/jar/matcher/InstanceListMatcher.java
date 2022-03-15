package com.github.linyuzai.plugin.jar.matcher;

import com.github.linyuzai.plugin.core.context.PluginContext;
import com.github.linyuzai.plugin.core.matcher.AbstractPluginMatcher;
import com.github.linyuzai.plugin.core.matcher.GenericTypePluginMatcher;
import com.github.linyuzai.plugin.core.resolver.dependence.DependOnResolvers;
import com.github.linyuzai.plugin.jar.JarPlugin;
import com.github.linyuzai.plugin.jar.resolver.JarInstancePluginResolver;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Deprecated
@DependOnResolvers(JarInstancePluginResolver.class)
public abstract class InstanceListMatcher<T> extends GenericTypePluginMatcher<List<? extends T>> {

    @Override
    public boolean tryMatch(PluginContext context, Type type) {
        Collection<?> instances = context.get(JarPlugin.INSTANCES);
        List<?> matchedInstances = instances.stream()
                .filter(((Class<?>) type)::isInstance)
                .collect(Collectors.toList());
        if (matchedInstances.isEmpty()) {
            return false;
        }
        context.set(this, matchedInstances);
        return true;
    }

    public Class<List<? extends T>> getMatchingClass() {
        return null;
    }

    @Override
    public abstract void onMatched(List<? extends T> plugins);
}
