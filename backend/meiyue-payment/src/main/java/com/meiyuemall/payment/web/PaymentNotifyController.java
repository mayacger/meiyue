package com.meiyuemall.payment.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.payment.domain.PaymentChannel;
import com.meiyuemall.payment.dto.PaymentResponse;
import com.meiyuemall.payment.service.PaymentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 支付回调与查单 API（I4）。
 * <p>
 * 公开回调（验签后处理，无需登录）：
 * <ul>
 *   <li>{@code POST /api/v1/payments/notify/mock}</li>
 *   <li>{@code POST /api/v1/payments/notify/wechat}</li>
 *   <li>{@code POST /api/v1/payments/notify/alipay}</li>
 * </ul>
 * 查单（需登录，运维/买家补偿）：{@code POST /api/v1/payments/{paymentNo}/query}
 * </p>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/payments")
public class PaymentNotifyController {

    private final PaymentService paymentService;

    public PaymentNotifyController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(value = "/notify/mock", consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String notifyMock(@RequestBody(required = false) String body, HttpServletRequest request) {
        return paymentService.handleNotify(PaymentChannel.MOCK, body == null ? "" : body, headerMap(request));
    }

    @PostMapping(value = "/notify/wechat", consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String notifyWechat(@RequestBody(required = false) String body, HttpServletRequest request) {
        return paymentService.handleNotify(PaymentChannel.WECHAT, body == null ? "" : body, headerMap(request));
    }

    @PostMapping(value = "/notify/alipay", consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String notifyAlipay(@RequestBody(required = false) String body, HttpServletRequest request) {
        return paymentService.handleNotify(PaymentChannel.ALIPAY, body == null ? "" : body, headerMap(request));
    }

    @PostMapping("/{paymentNo}/query")
    public ApiResponse<PaymentResponse> query(@PathVariable String paymentNo) {
        return ApiResponse.ok(paymentService.queryAndSync(paymentNo));
    }

    @GetMapping("/{paymentNo}")
    public ApiResponse<PaymentResponse> get(@PathVariable String paymentNo) {
        return ApiResponse.ok(paymentService.getByPaymentNo(paymentNo));
    }

    private static Map<String, String> headerMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        if (names != null) {
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                map.put(name, request.getHeader(name));
            }
        }
        return map;
    }
}
