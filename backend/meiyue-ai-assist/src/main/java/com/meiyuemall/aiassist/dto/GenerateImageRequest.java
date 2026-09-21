package com.meiyuemall.aiassist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 单独出图请求 */
public record GenerateImageRequest(
        @NotBlank @Size(max = 512) String prompt
) {}
