package com.github.linyuzai.domain.core.proxy;

import com.github.linyuzai.domain.core.DomainContext;
import com.github.linyuzai.domain.core.DomainObject;
import com.github.linyuzai.domain.core.schrodinger.SchrodingerDomainObject;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.Method;

/**
 * 薛定谔模型代理
 */
@Getter
public class ProxySchrodingerDomainObject<T extends DomainObject> extends SchrodingerDomainObject<T> implements DomainObject, DomainProxy {

    protected final Class<T> type;

    public ProxySchrodingerDomainObject(Class<T> type, @NonNull String id, @NonNull DomainContext context) {
        super(id, context);
        this.type = type;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //如果是 getId 则直接返回
        if ("getId".equals(method.getName())) {
            return id;
        }
        return method.invoke(getTarget(), args);
    }

    @Override
    protected Class<? extends T> getDomainObjectType() {
        return type;
    }
}
