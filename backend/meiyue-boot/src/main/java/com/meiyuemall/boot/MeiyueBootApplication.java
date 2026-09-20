package com.meiyuemall.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * 美月商城 API 启动入口。
 * <p>
 * 扫描范围：{@code com.meiyuemall} 下全部模块（common / identity / tenant / 各业务域）。
 * </p>
 * <p>
 * 脚手架阶段排除数据源 / JPA / Redis 自动配置，避免本地未起 PostgreSQL/Redis 时无法启动。
 * I1 起删除对应 exclude，并在 application.yml 填入真实连接。
 * </p>
 */
@SpringBootApplication(
        scanBasePackages = "com.meiyuemall",
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                RedisAutoConfiguration.class
        }
)
public class MeiyueBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeiyueBootApplication.class, args);
    }
}
