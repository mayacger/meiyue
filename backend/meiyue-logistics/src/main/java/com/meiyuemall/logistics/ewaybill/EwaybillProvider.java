package com.meiyuemall.logistics.ewaybill;

/**
 * 电子面单通道端口（I11）。默认 MOCK；真实快递面单二期。
 */
public interface EwaybillProvider {

    /**
     * @param carrierCode 承运商编码
     * @param orderId     订单 ID
     * @param packageSeq  包裹序号
     */
    EwaybillResult print(String carrierCode, Long orderId, int packageSeq);

    record EwaybillResult(String provider, String ewaybillNo, String labelUrl) {}
}
