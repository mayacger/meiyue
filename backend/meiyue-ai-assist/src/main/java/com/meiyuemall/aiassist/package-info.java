/**
 * AI 素材辅助模块（I8）：
 * <ul>
 *   <li>可插拔 {@link com.meiyuemall.aiassist.provider.AiImageProvider} /
 *       {@link com.meiyuemall.aiassist.provider.AiDetailProvider}（必有 MOCK）</li>
 *   <li>素材入库 + {@link com.meiyuemall.aiassist.safety.ContentSafetyPort} 审核占位</li>
 *   <li>挂接商家商品封面/详情；失败降级人工上传</li>
 * </ul>
 * <b>硬约束：</b>不做推广视频、不做直播。
 * <p>入口见模块 README 与 {@link com.meiyuemall.aiassist.web.AiAssistController}。</p>
 */
package com.meiyuemall.aiassist;
