package com.github.linyuzai.plugin.jar;

import com.github.linyuzai.plugin.core.concept.AbstractPluginConcept;
import com.github.linyuzai.plugin.core.concept.Plugin;
import com.github.linyuzai.plugin.core.context.PluginContextFactory;
import com.github.linyuzai.plugin.core.event.*;
import com.github.linyuzai.plugin.core.factory.PluginFactory;
import com.github.linyuzai.plugin.core.filter.PluginFilter;
import com.github.linyuzai.plugin.core.matcher.PluginMatcher;
import com.github.linyuzai.plugin.core.resolver.*;
import com.github.linyuzai.plugin.jar.classloader.JarPluginClassLoader;
import com.github.linyuzai.plugin.jar.factory.JarFilePluginFactory;
import com.github.linyuzai.plugin.jar.factory.JarPathPluginFactory;
import com.github.linyuzai.plugin.jar.factory.JarURLPluginFactory;
import com.github.linyuzai.plugin.jar.matcher.JarDynamicPluginMatcher;
import com.github.linyuzai.plugin.jar.resolver.JarBytesPluginResolver;
import com.github.linyuzai.plugin.jar.resolver.JarFileNamePluginResolver;
import com.github.linyuzai.plugin.jar.resolver.JarPropertiesPluginResolver;

import java.util.*;

public class JarPluginConcept extends AbstractPluginConcept {

    private final Collection<JarPluginClassLoader> classLoaders =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public JarPluginConcept(PluginContextFactory pluginContextFactory,
                            PluginEventPublisher pluginEventPublisher,
                            Collection<PluginFactory> pluginFactories,
                            Collection<PluginResolver> pluginResolvers,
                            Collection<PluginFilter> pluginFilters,
                            Collection<PluginMatcher> pluginMatchers) {
        super(pluginContextFactory, pluginEventPublisher, pluginFactories,
                pluginResolvers, pluginFilters, pluginMatchers);
    }

    public Collection<JarPluginClassLoader> getClassLoaders() {
        return classLoaders;
    }

    @Override
    public Plugin load(Object o) {
        Plugin plugin = super.load(o);
        if (plugin instanceof JarPlugin) {
            JarPluginClassLoader classLoader = ((JarPlugin) plugin).getClassLoader();
            classLoaders.add(classLoader);
        }
        return plugin;
    }

    public static class Builder extends AbstractBuilder<Builder> {

        private ClassLoader classLoader;

        public Builder() {
            mappingResolver(BytesPluginResolver.class, JarBytesPluginResolver.class);
            mappingResolver(FileNamePluginResolver.class, JarFileNamePluginResolver.class);
            mappingResolver(PropertiesPluginResolver.class, JarPropertiesPluginResolver.class);
        }

        public Builder classLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public Builder match(Object callback) {
            addMatchers(new JarDynamicPluginMatcher(callback));
            return this;
        }

        public JarPluginConcept build() {

            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }

            addFactory(new JarPathPluginFactory(classLoader));
            addFactory(new JarFilePluginFactory(classLoader));
            addFactory(new JarURLPluginFactory(classLoader));

            preBuild();

            return new JarPluginConcept(
                    pluginContextFactory,
                    pluginEventPublisher,
                    pluginFactories,
                    pluginResolvers,
                    pluginFilters,
                    pluginMatchers);
        }
    }
}
