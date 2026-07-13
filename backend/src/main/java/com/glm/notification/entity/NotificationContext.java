package com.glm.notification.entity;

import com.glm.notification.NotificationPublisher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Gives NotificationEntityListener (a plain object instantiated by JPA, not a Spring bean)
 * access to the NotificationPublisher bean. Spring Framework 6 removed
 * SpringBeanAutowiringSupport, so entity listeners can no longer self-autowire directly.
 */
@Component
public class NotificationContext implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    static NotificationPublisher publisher() {
        return context == null ? null : context.getBean(NotificationPublisher.class);
    }
}
