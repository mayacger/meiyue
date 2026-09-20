import { PageShell } from "@meiyue/ui";

/**
 * 平台运营总览占位
 * 后续：入驻审核、类目/费率、风控与争议工单。
 */
export function OverviewPage() {
  return (
    <PageShell
      title="平台后台"
      subtitle="入驻审核、类目费率、风控争议与全局配置。本轮仅脚手架。"
    >
      <ul>
        <li>工程标识：@meiyue/web-admin</li>
        <li>下一迭代：I1 入驻审核与平台账号 RBAC</li>
        <li>跨租户治理：平台不伪装成商家租户</li>
      </ul>
    </PageShell>
  );
}
