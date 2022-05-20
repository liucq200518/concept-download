package com.github.linyuzai.connection.loadbalance.autoconfigure;

import com.github.linyuzai.connection.loadbalance.core.concept.ErrorHandler;
import com.github.linyuzai.connection.loadbalance.core.event.ConnectionEventPublisher;
import com.github.linyuzai.connection.loadbalance.core.logger.ErrorLogger;
import com.github.linyuzai.connection.loadbalance.core.server.ConnectionServerProvider;
import com.github.linyuzai.connection.loadbalance.core.extension.ScheduledExecutorServiceFactory;
import com.github.linyuzai.connection.loadbalance.core.extension.SingleThreadScheduledExecutorServiceFactory;
import com.github.linyuzai.connection.loadbalance.discovery.DiscoveryConnectionServerProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

@Configuration(proxyBeanMethods = false)
public class ConnectionLoadBalanceConfiguration {

    @Bean
    public ConnectionScopeRegister connectionScopeRegister() {
        return new ConnectionScopeRegister();
    }

    @Bean
    @ConditionalOnMissingBean
    public ScopeHelper scopeHelper(GenericApplicationContext context) {
        return new ScopeHelper(context);
    }

    @Bean
    @ConnectionScope
    @ConditionalOnMissingBean
    @ConditionalOnClass({DiscoveryClient.class, Registration.class})
    public ConnectionServerProvider connectionServerProvider(DiscoveryClient client, Registration registration) {
        return new DiscoveryConnectionServerProvider(client, registration);
    }

    @Bean
    @ConnectionScope
    @ConditionalOnMissingBean
    public ErrorHandler errorHandler() {
        Log log = LogFactory.getLog(ErrorLogger.class);
        return new ErrorLogger(log::info, log::error);
    }

    @Bean
    @ConnectionScope
    @ConditionalOnMissingBean
    public ConnectionEventPublisher connectionEventPublisher(ApplicationEventPublisher publisher) {
        return new ApplicationConnectionEventPublisher(publisher);
    }

    @Bean
    @ConnectionScope
    @ConditionalOnMissingBean
    public ScheduledExecutorServiceFactory scheduledExecutorServiceFactory() {
        return new SingleThreadScheduledExecutorServiceFactory();
    }
}
