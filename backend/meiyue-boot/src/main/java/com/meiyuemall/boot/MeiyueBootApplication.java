package com.meiyuemall.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 美月商城 API 启动入口。
 * <p>I9：启用 Redis（延迟队列 / 限流 / 可选 AI 队列）；DB 扫描仍可作兜底。</p>
 */
@SpringBootApplication(scanBasePackages = "com.meiyuemall")
@EnableJpaRepositories(basePackages = "com.meiyuemall")
@EntityScan(basePackages = "com.meiyuemall")
@EnableScheduling
public class MeiyueBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeiyueBootApplication.class, args);
    }
}
