package com.meiyuemall.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 支付通道配置（密钥仅来自配置/环境变量，禁止入库明文）。
 * <p>绑定前缀 {@code meiyue.payment}。</p>
 *
 * @param defaultChannel 默认通道：MOCK / WECHAT / ALIPAY
 * @param mockEnabled    是否允许 MOCK（本地/联调）
 * @param wechat         微信商户配置占位
 * @param alipay         支付宝商户配置占位
 */
@ConfigurationProperties(prefix = "meiyue.payment")
public record PaymentProperties(
        String defaultChannel,
        boolean mockEnabled,
        Wechat wechat,
        Alipay alipay
) {
    /** 微信：appId/mchId/apiV3Key/privateKeyPem 均应从环境变量注入 */
    public record Wechat(String appId, String mchId, String apiV3Key, String privateKeyPem, String notifyUrl) {}
    /** 支付宝：appId/privateKey/alipayPublicKey 均应从环境变量注入 */
    public record Alipay(String appId, String privateKey, String alipayPublicKey, String notifyUrl) {}
}
