package com.meiyuemall.boot.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * I7：业务侧自定义计数器骨架（支付成功、AI 调用等可后续挂接）。
 */
@Configuration
public class MetricsConfig {

    public static final String PAYMENT_SUCCESS = "meiyue.payment.success";
    public static final String AI_GENERATE = "meiyue.ai.generate";

    @Bean
    Counter meiyuePaymentSuccessCounter(MeterRegistry registry) {
        return Counter.builder(PAYMENT_SUCCESS)
                .description("支付成功次数（骨架，业务侧可 increment）")
                .register(registry);
    }

    @Bean
    Counter meiyueAiGenerateCounter(MeterRegistry registry) {
        return Counter.builder(AI_GENERATE)
                .description("AI 生成调用次数（骨架）")
                .register(registry);
    }
}
