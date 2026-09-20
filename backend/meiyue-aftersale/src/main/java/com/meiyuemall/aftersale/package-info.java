/**
 * 售后模块（I6）：
 * <ul>
 *   <li>仅退款 / 退货退款</li>
 *   <li>商家审核与 48h 超时自动同意（{@link com.meiyuemall.aftersale.job.AftersaleAutoApproveJob}）</li>
 *   <li>退款写入结算账本（非通道原路退）</li>
 * </ul>
 * 入口见模块 {@code README.md} 与 {@link com.meiyuemall.aftersale.web.AftersaleController}。
 */
package com.meiyuemall.aftersale;
