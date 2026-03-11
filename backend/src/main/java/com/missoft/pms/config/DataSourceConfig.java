package com.missoft.pms.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    @Bean
    public static BeanPostProcessor userContextDataSourcePostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof javax.sql.DataSource dataSource && !(bean instanceof UserContextDataSource)) {
                    return new UserContextDataSource(dataSource);
                }
                return bean;
            }
        };
    }
}
