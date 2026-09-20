package com.meiyuemall.logistics.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "shipment_tracks")
public class ShipmentTrack {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;
    @Column(nullable = false, length = 32)
    private String status;
    @Column(nullable = false, length = 512)
    private String description;
    @Column(nullable = false, length = 16)
    private String source = "MANUAL";
    @Column(name = "tracked_at", nullable = false)
    private Instant trackedAt = Instant.now();
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getTrackedAt() { return trackedAt; }
    public void setTrackedAt(Instant trackedAt) { this.trackedAt = trackedAt; }
}
