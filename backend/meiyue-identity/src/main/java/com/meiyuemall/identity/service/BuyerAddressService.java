package com.meiyuemall.identity.service;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.identity.domain.BuyerAddress;
import com.meiyuemall.identity.dto.AddressResponse;
import com.meiyuemall.identity.dto.AddressUpsertRequest;
import com.meiyuemall.identity.repo.BuyerAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 买家地址簿服务（I18）。
 * <p>所有操作绑定当前登录 userId，防越权。</p>
 */
@Service
public class BuyerAddressService {

    private final BuyerAddressRepository addressRepository;

    public BuyerAddressService(BuyerAddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> listMine() {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        return addressRepository.findByUserIdOrderByDefaultAddressDescIdDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AddressResponse create(AddressUpsertRequest request) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        boolean makeDefault = Boolean.TRUE.equals(request.defaultAddress())
                || addressRepository.findByUserIdOrderByDefaultAddressDescIdDesc(userId).isEmpty();
        if (makeDefault) {
            addressRepository.clearDefaultForUser(userId);
        }
        BuyerAddress addr = new BuyerAddress();
        addr.setUserId(userId);
        apply(addr, request, makeDefault);
        addressRepository.save(addr);
        return toResponse(addr);
    }

    @Transactional
    public AddressResponse update(Long id, AddressUpsertRequest request) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        BuyerAddress addr = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "地址不存在"));
        boolean makeDefault = Boolean.TRUE.equals(request.defaultAddress());
        if (makeDefault) {
            addressRepository.clearDefaultForUser(userId);
        }
        apply(addr, request, makeDefault || addr.isDefaultAddress());
        if (!makeDefault && Boolean.FALSE.equals(request.defaultAddress())) {
            addr.setDefaultAddress(false);
        }
        return toResponse(addr);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        BuyerAddress addr = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "地址不存在"));
        addressRepository.delete(addr);
    }

    @Transactional
    public AddressResponse setDefault(Long id) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        BuyerAddress addr = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "地址不存在"));
        addressRepository.clearDefaultForUser(userId);
        addr.setDefaultAddress(true);
        return toResponse(addr);
    }

    private void apply(BuyerAddress addr, AddressUpsertRequest request, boolean defaultAddress) {
        addr.setReceiverName(request.receiverName().trim());
        addr.setReceiverPhone(request.receiverPhone().trim());
        addr.setProvince(request.province().trim());
        addr.setCity(request.city().trim());
        addr.setDistrict(request.district().trim());
        addr.setDetailAddress(request.detailAddress().trim());
        addr.setDefaultAddress(defaultAddress);
    }

    private AddressResponse toResponse(BuyerAddress a) {
        return new AddressResponse(
                a.getId(),
                a.getReceiverName(),
                a.getReceiverPhone(),
                a.getProvince(),
                a.getCity(),
                a.getDistrict(),
                a.getDetailAddress(),
                a.isDefaultAddress()
        );
    }
}
