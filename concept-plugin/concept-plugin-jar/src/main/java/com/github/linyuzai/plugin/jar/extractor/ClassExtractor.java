package com.github.linyuzai.plugin.jar.extractor;

import com.github.linyuzai.plugin.core.extractor.TypeMetadataPluginExtractor;
import com.github.linyuzai.plugin.core.matcher.PluginMatcher;
import com.github.linyuzai.plugin.core.util.ReflectionUtils;
import com.github.linyuzai.plugin.core.util.TypeMetadata;
import com.github.linyuzai.plugin.jar.matcher.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

public abstract class ClassExtractor<T> extends TypeMetadataPluginExtractor<T> {

    @Override
    public PluginMatcher bind(TypeMetadata metadata, Type type) {
        Type target = metadata.getType();
        Class<?> targetClass = getTargetClass(target);
        if (targetClass == null) {
            return null;
        }
        if (metadata.isMap()) {
            return new ClassMapMatcher(metadata.getMapClass(), targetClass);
        } else if (metadata.isList()) {
            return new ClassListMatcher(metadata.getListClass(), targetClass);
        } else if (metadata.isSet()) {
            return new ClassSetMatcher(metadata.getSetClass(), targetClass);
        } else if (metadata.isCollection()) {
            return new ClassListMatcher(metadata.getCollectionClass(), targetClass);
        } else if (metadata.isArray()) {
            return new ClassArrayMatcher(targetClass);
        } else {
            return new ClassObjectMatcher(targetClass);
        }
    }

    public Class<?> getTargetClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                if (Class.class.isAssignableFrom((Class<?>) rawType)) {
                    Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
                    return ReflectionUtils.toClass(arguments[0]);
                }
            }
        } else if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length > 0) {
                Type upperBound = upperBounds[0];
                if (upperBound instanceof Class) {
                    if (Class.class.isAssignableFrom((Class<?>) upperBound)) {
                        return Object.class;
                    }
                } else if (upperBound instanceof ParameterizedType) {
                    return getTargetClass(upperBound);
                }
            }
        }
        return null;
    }
}
