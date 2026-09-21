package com.meiyuemall.trade.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.trade.dto.CartAddRequest;
import com.meiyuemall.trade.dto.CartItemResponse;
import com.meiyuemall.trade.dto.CheckoutRequest;
import com.meiyuemall.trade.dto.FreightEstimateResponse;
import com.meiyuemall.trade.dto.OrderResponse;
import com.meiyuemall.trade.service.CartService;
import com.meiyuemall.trade.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 买家交易 API（I3 + I31）。
 * <ul>
 *   <li>购物车：/api/v1/buyer/cart</li>
 *   <li>运费预估：POST /api/v1/buyer/orders/freight-estimate</li>
 *   <li>下单：POST /api/v1/buyer/orders/checkout</li>
 *   <li>模拟支付：POST /api/v1/buyer/orders/{id}/mock-pay</li>
 * </ul>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/buyer")
@PreAuthorize("isAuthenticated()")
public class BuyerTradeController {

    private final CartService cartService;
    private final OrderService orderService;

    public BuyerTradeController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping("/cart")
    public ApiResponse<List<CartItemResponse>> cart() {
        return ApiResponse.ok(cartService.listMine());
    }

    @PostMapping("/cart/items")
    public ApiResponse<CartItemResponse> addCart(@Valid @RequestBody CartAddRequest request) {
        return ApiResponse.ok(cartService.add(request));
    }

    @DeleteMapping("/cart/items/{id}")
    public ApiResponse<Void> removeCart(@PathVariable Long id) {
        cartService.remove(id);
        return ApiResponse.ok(null);
    }

    /**
     * I31：结算页运费预估。
     * body 可空；有 {@code cartItemIds} 时仅估算所选行，否则整车。
     */
    @PostMapping("/orders/freight-estimate")
    public ApiResponse<FreightEstimateResponse> freightEstimate(
            @RequestBody(required = false) CheckoutRequest request
    ) {
        List<Long> ids = request == null ? null : request.cartItemIds();
        return ApiResponse.ok(orderService.estimateFreightForCart(ids));
    }

    @PostMapping("/orders/checkout")
    public ApiResponse<OrderResponse> checkout(@RequestBody(required = false) CheckoutRequest request) {
        return ApiResponse.ok(orderService.checkout(request));
    }

    @GetMapping("/orders")
    public ApiResponse<List<OrderResponse>> orders() {
        return ApiResponse.ok(orderService.listMine());
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<OrderResponse> order(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getMine(id));
    }

    @PostMapping("/orders/{id}/mock-pay")
    public ApiResponse<OrderResponse> mockPay(@PathVariable Long id) {
        return ApiResponse.ok(orderService.mockPay(id));
    }

    @PostMapping("/orders/{id}/confirm-receipt")
    public ApiResponse<OrderResponse> confirmReceipt(@PathVariable Long id) {
        return ApiResponse.ok(orderService.confirmReceipt(id));
    }

    /** I32：买家取消未支付订单（释放预占库存） */
    @PostMapping("/orders/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(@PathVariable Long id) {
        return ApiResponse.ok(orderService.cancelMine(id));
    }
}
