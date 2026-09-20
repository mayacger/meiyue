package com.meiyuemall.aiassist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 单独生成详情请求 */
public record GenerateDetailRequest(
        @NotBlank @Size(max = 256) String title,
        @Size(max = 512) String hints
) {}
