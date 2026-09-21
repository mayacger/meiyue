package com.meiyuemall.logistics.ewaybill;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * MOCK 电子面单：生成面单号与占位打印 URL（非真实快递接口）。
 */
@Component
public class MockEwaybillProvider implements EwaybillProvider {

    @Override
    public EwaybillResult print(String carrierCode, Long orderId, int packageSeq) {
        String no = "EB" + (carrierCode == null ? "XX" : carrierCode.toUpperCase())
                + orderId + "P" + packageSeq
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        String url = "mock://ewaybill/" + no + ".pdf";
        return new EwaybillResult("MOCK", no, url);
    }
}
