package com.meiyuemall.logistics.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateShipmentStatusRequest(@NotBlank String status, String description) {}
