package com.meiyuemall.aiassist.dto;

/** 详情生成响应（可未入库，仅返回文案供表单回填） */
public record DetailGenerateResponse(
        boolean success,
        String provider,
        String titleSuggest,
        String detailHtml,
        String message,
        boolean allowManualUpload
) {}
