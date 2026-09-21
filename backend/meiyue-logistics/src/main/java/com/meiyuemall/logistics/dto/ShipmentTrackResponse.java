package com.meiyuemall.logistics.dto;

public record ShipmentTrackResponse(Long id, String status, String description, String source, String trackedAt) {}
