package com.missoft.pms.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

    @Autowired
    private AuditUserHibernateInterceptor auditUserHibernateInterceptor;

    @Bean
    public HibernatePropertiesCustomizer auditUserHibernatePropertiesCustomizer() {
        return hibernateProperties -> hibernateProperties.put(AvailableSettings.INTERCEPTOR, auditUserHibernateInterceptor);
    }
}
