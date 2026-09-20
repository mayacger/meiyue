import { PageShell } from "@meiyue/ui";

/**
 * 商家后台首页占位
 * 后续：入驻状态、待发货、售后待审、装修草稿入口。
 */
export function DashboardPage() {
  return (
    <PageShell
      title="商家后台"
      subtitle="装修、商品、履约、正逆向物流与售后。本轮仅脚手架。"
    >
      <ul>
        <li>工程标识：@meiyue/web-seller</li>
        <li>下一迭代：I1 开店；I2 商品发布与模板装修</li>
        <li>支付：商家侧 MVP 无独立支付账户（平台代收）</li>
      </ul>
    </PageShell>
  );
}
