package com.meiyuemall.identity.dto;

/**
 * 图形验证码挑战（I35）。
 *
 * @param enabled     是否启用（关闭时其余字段为空）
 * @param captchaId   挑战 ID
 * @param imageBase64 data URI（SVG）
 */
public record CaptchaChallengeResponse(
        boolean enabled,
        String captchaId,
        String imageBase64
) {}
