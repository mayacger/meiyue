package com.meiyuemall.identity.service;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.identity.domain.BuyerInvoiceProfile;
import com.meiyuemall.identity.dto.InvoiceProfileResponse;
import com.meiyuemall.identity.dto.InvoiceProfileUpsertRequest;
import com.meiyuemall.identity.repo.BuyerInvoiceProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 买家发票抬头 CRUD（I33 占位）。
 */
@Service
public class InvoiceProfileService {

    private final BuyerInvoiceProfileRepository repository;

    public InvoiceProfileService(BuyerInvoiceProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<InvoiceProfileResponse> listMine() {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        return repository.findByUserIdOrderByDefaultProfileDescIdDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public InvoiceProfileResponse create(InvoiceProfileUpsertRequest request) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        BuyerInvoiceProfile p = new BuyerInvoiceProfile();
        p.setUserId(userId);
        apply(p, request);
        if (Boolean.TRUE.equals(request.defaultProfile())) {
            repository.clearDefault(userId);
            p.setDefaultProfile(true);
        }
        repository.save(p);
        return toResponse(p);
    }

    @Transactional
    public InvoiceProfileResponse update(Long id, InvoiceProfileUpsertRequest request) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        BuyerInvoiceProfile p = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "发票抬头不存在"));
        apply(p, request);
        if (Boolean.TRUE.equals(request.defaultProfile())) {
            repository.clearDefault(userId);
            p.setDefaultProfile(true);
        }
        return toResponse(p);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        BuyerInvoiceProfile p = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "发票抬头不存在"));
        repository.delete(p);
    }

    @Transactional
    public InvoiceProfileResponse setDefault(Long id) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        BuyerInvoiceProfile p = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "发票抬头不存在"));
        repository.clearDefault(userId);
        p.setDefaultProfile(true);
        return toResponse(p);
    }

    private void apply(BuyerInvoiceProfile p, InvoiceProfileUpsertRequest request) {
        p.setTitle(request.title().trim());
        p.setTaxNo(request.taxNo() == null || request.taxNo().isBlank() ? null : request.taxNo().trim());
        String type = request.invoiceType() == null || request.invoiceType().isBlank()
                ? "PERSONAL" : request.invoiceType().trim().toUpperCase();
        if (!"PERSONAL".equals(type) && !"COMPANY".equals(type)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "invoiceType 须为 PERSONAL 或 COMPANY");
        }
        p.setInvoiceType(type);
    }

    private InvoiceProfileResponse toResponse(BuyerInvoiceProfile p) {
        return new InvoiceProfileResponse(
                p.getId(), p.getTitle(), p.getTaxNo(), p.getInvoiceType(), p.isDefaultProfile()
        );
    }
}
