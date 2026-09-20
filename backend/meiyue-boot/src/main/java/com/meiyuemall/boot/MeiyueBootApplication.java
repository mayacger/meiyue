package com.meiyuemall.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 美月商城 API 启动入口。
 * <p>
 * I1+：DataSource / JPA / Flyway；I3：启用 Scheduling（支付超时关单）。
 * 仍排除 Redis（延迟队列可后续切换）。
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
@EnableScheduling
public class MeiyueBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeiyueBootApplication.class, args);
    }
}
