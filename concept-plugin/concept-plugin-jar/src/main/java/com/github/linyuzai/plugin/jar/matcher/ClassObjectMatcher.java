package com.github.linyuzai.plugin.jar.matcher;

import com.github.linyuzai.plugin.core.exception.PluginException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassObjectMatcher extends ClassMatcher {

    public ClassObjectMatcher(Class<?> target) {
        super(target);
    }

    @Override
    public Object map(Map<String, Object> map) {
        List<?> list = new ArrayList<>(map.values());
        if (map.size() > 1) {
            throw new PluginException("More than one class matched: " + list);
        }
        return list.get(0);
    }
}
