package com.meiyuemall.identity.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * IAM 基础 Bean：密码编码器、JWT 配置启用。
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class IdentityConfig {

    /**
     * BCrypt 密码哈希（规划 §5.2）。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
