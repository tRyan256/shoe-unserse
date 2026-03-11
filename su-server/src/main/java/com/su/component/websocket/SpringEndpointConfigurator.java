package com.su.component.websocket;

import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * WebSocket 端点配置器，用于在 WebSocket 中注入 Spring Bean
 */
@Component
public class SpringEndpointConfigurator extends ServerEndpointConfig.Configurator implements ApplicationContextAware {

    private static volatile ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringEndpointConfigurator.applicationContext = applicationContext;
    }

    @Override
    public boolean checkOrigin(String originHeaderValue) {
        return true;
    }

    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        if (applicationContext == null) {
            return super.getEndpointInstance(clazz);
        }
        try {
            BeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
            return beanFactory.getBean(clazz);
        } catch (BeansException e) {
            return super.getEndpointInstance(clazz);
        }
    }
}