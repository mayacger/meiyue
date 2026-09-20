package com.meiyuemall.decoration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveDraftRequest(
        @NotBlank String templateCode,
        @NotBlank @Size(max = 32) String themeColor,
        @NotBlank String floorsJson
) {}
