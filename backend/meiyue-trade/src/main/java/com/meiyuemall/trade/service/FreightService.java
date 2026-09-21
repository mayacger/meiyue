package com.meiyuemall.trade.service;

import com.meiyuemall.tenant.domain.Store;
import com.meiyuemall.tenant.repo.StoreRepository;
import com.meiyuemall.trade.dto.FreightEstimateResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 运费模板计算（I31 MVP）。
 * <p>按店汇总商品小计：达包邮门槛则运费 0，否则用店铺默认 freightCents。多店累加。</p>
 */
@Service
public class FreightService {

    private final StoreRepository storeRepository;

    public FreightService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    /**
     * @param tenantGoodsCents 租户 ID → 本店商品小计（分）
     */
    @Transactional(readOnly = true)
    public FreightEstimateResponse estimate(Map<Long, Long> tenantGoodsCents) {
        long goodsTotal = 0;
        long freightTotal = 0;
        List<FreightEstimateResponse.ShopFreight> shops = new ArrayList<>();
        for (Map.Entry<Long, Long> e : tenantGoodsCents.entrySet()) {
            Long tenantId = e.getKey();
            long goods = Math.max(0, e.getValue());
            goodsTotal += goods;
            Store store = storeRepository.findByTenantId(tenantId).orElse(null);
            long defaultFreight = store == null ? 0 : Math.max(0, store.getFreightCents());
            Long threshold = store == null ? null : store.getFreeShippingThresholdCents();
            boolean free = threshold != null && goods >= threshold;
            long freight = free ? 0 : defaultFreight;
            freightTotal += freight;
            shops.add(new FreightEstimateResponse.ShopFreight(
                    tenantId,
                    store == null ? ("店铺#" + tenantId) : store.getName(),
                    goods,
                    freight,
                    free,
                    threshold
            ));
        }
        return new FreightEstimateResponse(goodsTotal, freightTotal, goodsTotal + freightTotal, shops);
    }

    /** 便捷：从行列表聚合 */
    @Transactional(readOnly = true)
    public FreightEstimateResponse estimateFromLines(List<Line> lines) {
        Map<Long, Long> map = new LinkedHashMap<>();
        for (Line line : lines) {
            map.merge(line.tenantId(), line.lineTotalCents(), Long::sum);
        }
        return estimate(map);
    }

    public record Line(Long tenantId, long lineTotalCents) {}
}
