package com.missoft.pms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 安全配置类
 * 配置认证和授权规则
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    /**
     * 配置安全过滤器链
     * 
     * @param http HttpSecurity对象
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护（因为我们使用JWT）
            .csrf(csrf -> csrf.disable())
            
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置会话管理为无状态
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置授权规则
            .authorizeHttpRequests(authz -> authz
                // 允许访问认证相关的接口
                .requestMatchers("/api/auth/**").permitAll()
                // 允许访问健康检查接口
                .requestMatchers("/api/health/**").permitAll()
                // 允许访问静态资源
                .requestMatchers("/static/**", "/public/**").permitAll()
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
    
    /**
     * 密码编码器Bean
     * 
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * CORS配置源
     * 
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许发送凭证
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}