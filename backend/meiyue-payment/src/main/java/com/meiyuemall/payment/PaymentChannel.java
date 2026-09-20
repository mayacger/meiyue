package com.meiyuemall.payment;

/**
 * 支付通道枚举（占位）。
 * <p>
 * 规划 C9 / C18：中国大陆主通道为微信与支付宝；
 * MVP 平台代收 + 周期结算账本；正式接入官方分账。
 * 本轮不做真实对接。
 * </p>
 */
public enum PaymentChannel {

    /** 微信支付（JSAPI / Native / App 等具体方式后续细分） */
    WECHAT,

    /** 支付宝（电脑网站 / 手机网站 / App 等后续细分） */
    ALIPAY
}
