package com.meiyuemall.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 美月商城 API 启动入口。
 * <p>
 * I1：启用 DataSource / JPA / Flyway；仍排除 Redis（I3 前可不依赖）。
 * 扫描 {@code com.meiyuemall} 下全部模块。
 * </p>
 */
@SpringBootApplication(
        scanBasePackages = "com.meiyuemall",
        exclude = {
                RedisAutoConfiguration.class
        }
)
@EnableJpaRepositories(basePackages = "com.meiyuemall")
@EntityScan(basePackages = "com.meiyuemall")
public class MeiyueBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeiyueBootApplication.class, args);
    }
}
