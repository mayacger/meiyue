package com.meiyuemall.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 美月商城 Worker 启动入口。
 * <p>
 * 职责（规划 §1.3）：定时/延迟/异步任务（关单 30min、售后 48h、AI 异步等）。
 * 本轮仅空壳进程 + Actuator 健康检查，不注册业务 Job。
 * </p>
 */
@SpringBootApplication(scanBasePackages = "com.meiyuemall.worker")
public class MeiyueWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeiyueWorkerApplication.class, args);
    }
}
