package com.meiyuemall.logistics.repo;

import com.meiyuemall.logistics.domain.Shipment;
import com.meiyuemall.logistics.domain.ShipmentDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    List<Shipment> findByTenantIdAndDirectionOrderByCreatedAtDesc(Long tenantId, ShipmentDirection direction);
    Optional<Shipment> findByIdAndTenantId(Long id, Long tenantId);
}
