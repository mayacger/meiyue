package com.meiyuemall.trade.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.trade.dto.OrderResponse;
import com.meiyuemall.trade.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 商家订单列表（发货入口） */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/seller/orders")
@PreAuthorize("hasAnyRole('SELLER_OWNER','SELLER_STAFF')")
public class SellerOrderController {

    private final OrderService orderService;

    public SellerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> list() {
        return ApiResponse.ok(orderService.listForSeller());
    }
}
