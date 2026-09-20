package com.meiyuemall.payment.service;

import com.meiyuemall.payment.domain.SettlementLedger;
import com.meiyuemall.payment.repo.SettlementLedgerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

/**
 * 周期结算账本（MVP）：支付成功后按订单行记 SALE；退款时记 REFUND（I6 挂接）。
 * 官方分账二期前不自动打款，仅记账可审计。
 */
@Service
public class SettlementLedgerService {

    private final SettlementLedgerRepository ledgerRepository;

    public SettlementLedgerService(SettlementLedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    /**
     * 为订单记一笔汇总 SALE（按租户拆分由调用方多次传入）。
     */
    @Transactional
    public void recordSale(Long tenantId, Long orderId, Long orderItemId, long amountCents, String remark) {
        if (orderItemId != null) {
            // 行级去重可后续加强；此处用 orderId+SALE 粗粒度防重复汇总时由 exists 控制
        }
        SettlementLedger row = new SettlementLedger();
        row.setTenantId(tenantId);
        row.setOrderId(orderId);
        row.setOrderItemId(orderItemId);
        row.setEntryType("SALE");
        row.setAmountCents(amountCents);
        row.setStatus("PENDING");
        row.setPeriodKey(currentPeriodKey());
        row.setRemark(remark);
        ledgerRepository.save(row);
    }

    @Transactional
    public void recordOrderSalesIfAbsent(Long orderId, List<SaleLine> lines) {
        if (ledgerRepository.existsByOrderIdAndEntryType(orderId, "SALE")) {
            return;
        }
        for (SaleLine line : lines) {
            recordSale(line.tenantId(), orderId, line.orderItemId(), line.amountCents(), line.remark());
        }
    }

    /**
     * 退款入账：金额为负，表示从商家账本扣回。
     */
    @Transactional
    public void recordRefund(Long tenantId, Long orderId, Long orderItemId, long refundCents, String remark) {
        SettlementLedger row = new SettlementLedger();
        row.setTenantId(tenantId);
        row.setOrderId(orderId);
        row.setOrderItemId(orderItemId);
        row.setEntryType("REFUND");
        row.setAmountCents(-Math.abs(refundCents));
        row.setStatus("PENDING");
        row.setPeriodKey(currentPeriodKey());
        row.setRemark(remark);
        ledgerRepository.save(row);
    }

    @Transactional(readOnly = true)
    public List<SettlementLedger> listPending(Long tenantId) {
        return ledgerRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, "PENDING");
    }

    /** 账期：yyyy-MM-Wn */
    public static String currentPeriodKey() {
        LocalDate today = LocalDate.now();
        WeekFields wf = WeekFields.of(Locale.CHINA);
        int week = today.get(wf.weekOfWeekBasedYear());
        return today.getYear() + "-W" + week;
    }

    public record SaleLine(Long tenantId, Long orderItemId, long amountCents, String remark) {}
}
