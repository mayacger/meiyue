package com.meiyuemall.logistics.repo;

import com.meiyuemall.logistics.domain.ShipmentTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShipmentTrackRepository extends JpaRepository<ShipmentTrack, Long> {
    List<ShipmentTrack> findByShipmentIdOrderByTrackedAtAsc(Long shipmentId);
}
