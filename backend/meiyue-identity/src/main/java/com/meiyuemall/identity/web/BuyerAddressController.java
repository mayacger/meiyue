package com.meiyuemall.identity.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.web.ApiResponse;
import com.meiyuemall.identity.dto.AddressResponse;
import com.meiyuemall.identity.dto.AddressUpsertRequest;
import com.meiyuemall.identity.service.BuyerAddressService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 买家地址簿 API（I18）。
 * <ul>
 *   <li>GET /api/v1/buyer/addresses</li>
 *   <li>POST /api/v1/buyer/addresses</li>
 *   <li>PUT /api/v1/buyer/addresses/{id}</li>
 *   <li>DELETE /api/v1/buyer/addresses/{id}</li>
 *   <li>POST /api/v1/buyer/addresses/{id}/default</li>
 * </ul>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX + "/buyer/addresses")
public class BuyerAddressController {

    private final BuyerAddressService addressService;

    public BuyerAddressController(BuyerAddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ApiResponse<List<AddressResponse>> list() {
        return ApiResponse.ok(addressService.listMine());
    }

    @PostMapping
    public ApiResponse<AddressResponse> create(@Valid @RequestBody AddressUpsertRequest request) {
        return ApiResponse.ok(addressService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AddressUpsertRequest request
    ) {
        return ApiResponse.ok(addressService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/default")
    public ApiResponse<AddressResponse> setDefault(@PathVariable Long id) {
        return ApiResponse.ok(addressService.setDefault(id));
    }
}
