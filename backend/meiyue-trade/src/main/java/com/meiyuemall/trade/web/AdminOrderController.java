package com.meiyuemall.trade.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.trade.service.OrderService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * 平台订单导出（I22）。
 * <p>GET /api/v1/admin/orders/export.csv — 全站已支付订单 CSV（无真实分账）。</p>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/admin/orders")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(value = "/export.csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportCsv() {
        String csv = orderService.exportPaidCsvForAdmin();
        byte[] body = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"admin-paid-orders.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }
}
