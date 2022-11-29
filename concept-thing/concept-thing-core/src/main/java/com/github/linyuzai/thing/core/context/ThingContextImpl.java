package com.github.linyuzai.thing.core.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class ThingContextImpl implements ThingContext {

    private final Map<Object, Object> map = new ConcurrentHashMap<>();

    @Override
    public <T> T get(Object key) {
        return (T) map.get(key);
    }

    @Override
    public void put(Object key, Object value) {
        map.put(key, value);
    }

    @Override
    public void remove(Object key) {
        map.remove(key);
    }
}
