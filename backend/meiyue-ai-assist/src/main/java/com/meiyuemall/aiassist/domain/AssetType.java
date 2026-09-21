package com.meiyuemall.aiassist.domain;

/**
 * 素材类型。I11 增加 VIDEO（推广短视频，非直播）。
 */
public enum AssetType {
    IMAGE,
    DETAIL_HTML,
    /** 推广短视频占位；禁止用于直播 */
    VIDEO
}
