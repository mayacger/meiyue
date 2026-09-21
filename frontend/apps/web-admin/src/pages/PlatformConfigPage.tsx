import { useEffect, useState } from "react";
import { PageContainer, ProCard, ProDescriptions, ProTable } from "@ant-design/pro-components";
import type { ProColumns } from "@ant-design/pro-components";
import { App, Button, Typography } from "antd";
import { apiFetch, downloadAuthenticated } from "@meiyue/api";
import type { PlatformConfigView } from "@meiyue/types";

/**
 * 平台运营配置只读（I23）+ 已支付订单导出（I22）
 * API：GET /platform/config · GET /admin/orders/export.csv
 * 不做：真实分账、密钥联调、费率写改
 */
export function PlatformConfigPage() {
  const { message } = App.useApp();
  const [cfg, setCfg] = useState<PlatformConfigView | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiFetch<PlatformConfigView>("/api/v1/platform/config")
      .then(setCfg)
      .catch((e) => message.error(e instanceof Error ? e.message : "加载失败"))
      .finally(() => setLoading(false));
  }, [message]);

  const feePct = cfg ? (Number(cfg.platformFeeRateBps) / 100).toFixed(2) : "-";
  const withdrawYuan = cfg ? (Number(cfg.minWithdrawCents) / 100).toFixed(2) : "-";

  const columns: ProColumns<PlatformConfigView["items"][number]>[] = [
    { title: "键", dataIndex: "key", width: 220 },
    { title: "值", dataIndex: "value", width: 120 },
    { title: "说明", dataIndex: "description", ellipsis: true },
    { title: "更新时间", dataIndex: "updatedAt", width: 200 }
  ];

  return (
    <PageContainer
      loading={loading}
      header={{
        title: "运营配置",
        subTitle: "费率只读展示 · 无真实分账打款",
        extra: [
          <Button
            key="export"
            type="primary"
            onClick={async () => {
              try {
                await downloadAuthenticated(
                  "/api/v1/admin/orders/export.csv",
                  "admin-paid-orders.csv"
                );
                message.success("已导出全站已支付订单");
              } catch (e) {
                message.error(e instanceof Error ? e.message : "导出失败");
              }
            }}
          >
            导出已支付订单 CSV
          </Button>
        ]
      }}
    >
      <ProCard gutter={16} wrap>
        <ProCard colSpan={8} title="服务费率" bordered>
          <Typography.Title level={3} style={{ margin: 0 }}>
            {feePct}%
          </Typography.Title>
          <Typography.Text type="secondary">
            {cfg?.platformFeeRateBps ?? "-"} bps · 只读
          </Typography.Text>
        </ProCard>
        <ProCard colSpan={8} title="结算周期" bordered>
          <Typography.Title level={3} style={{ margin: 0 }}>
            {cfg?.settlementCycle ?? "-"}
          </Typography.Title>
          <Typography.Text type="secondary">WEEKLY / MONTHLY · 只读</Typography.Text>
        </ProCard>
        <ProCard colSpan={8} title="最低提现" bordered>
          <Typography.Title level={3} style={{ margin: 0 }}>
            ¥{withdrawYuan}
          </Typography.Title>
          <Typography.Text type="secondary">只读展示，无打款联调</Typography.Text>
        </ProCard>
      </ProCard>
      <ProCard title="配置明细" style={{ marginTop: 16 }}>
        <ProDescriptions column={1} size="small">
          <ProDescriptions.Item label="说明">
            种子数据来自 Flyway V15；本页不可编辑，避免与真实分账联调混淆。
          </ProDescriptions.Item>
        </ProDescriptions>
        <ProTable
          search={false}
          pagination={false}
          rowKey="key"
          columns={columns}
          dataSource={cfg?.items ?? []}
          toolBarRender={false}
        />
      </ProCard>
    </PageContainer>
  );
}
