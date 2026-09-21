import { useEffect, useState } from "react";
import {
  ModalForm,
  PageContainer,
  ProCard,
  ProDescriptions,
  ProFormDigit,
  ProTable
} from "@ant-design/pro-components";
import type { ProColumns } from "@ant-design/pro-components";
import { App, Button, Typography } from "antd";
import { apiFetch, downloadAuthenticated } from "@meiyue/api";
import type { PlatformConfigView } from "@meiyue/types";

/**
 * 平台运营配置（I23 + I32）
 * API：GET /platform/config · PUT /admin/platform-config
 * I32：可改 auto_confirm_receipt_days；费率仍只读展示
 */
export function PlatformConfigPage() {
  const { message, modal } = App.useApp();
  const [cfg, setCfg] = useState<PlatformConfigView | null>(null);
  const [loading, setLoading] = useState(true);

  async function reload() {
    setCfg(await apiFetch<PlatformConfigView>("/api/v1/platform/config"));
  }

  useEffect(() => {
    reload()
      .catch((e) => message.error(e instanceof Error ? e.message : "加载失败"))
      .finally(() => setLoading(false));
  }, [message]);

  const feePct = cfg ? (Number(cfg.platformFeeRateBps) / 100).toFixed(2) : "-";
  const withdrawYuan = cfg ? (Number(cfg.minWithdrawCents) / 100).toFixed(2) : "-";
  const autoDays = cfg?.autoConfirmReceiptDays ?? "-";

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
        subTitle: "自动确认天数可改 · 费率只读 · 无真实分账",
        extra: [
          <ModalForm
            key="auto"
            title="修改自动确认天数"
            trigger={<Button>自动确认天数</Button>}
            initialValues={{ days: Number(autoDays) || 7 }}
            onFinish={async (values) => {
              try {
                const next = await apiFetch<PlatformConfigView>("/api/v1/admin/platform-config", {
                  method: "PUT",
                  json: {
                    key: "auto_confirm_receipt_days",
                    value: String(Math.max(0, Math.floor(Number(values.days))))
                  }
                });
                setCfg(next);
                message.success("已更新自动确认天数");
                return true;
              } catch (e) {
                message.error(e instanceof Error ? e.message : "更新失败");
                return false;
              }
            }}
          >
            <ProFormDigit
              name="days"
              label="签收后自动确认天数"
              min={0}
              extra="0 = 全部签收后立即确认；默认 7"
              rules={[{ required: true }]}
            />
          </ModalForm>,
          <Button
            key="export"
            type="primary"
            onClick={() => {
              // I35：导出二次确认
              modal.confirm({
                title: "确认导出全站已支付订单 CSV？",
                content: "包含全平台已支付订单明细，请妥善保管。",
                okText: "确认导出",
                cancelText: "取消",
                onOk: async () => {
                  try {
                    await downloadAuthenticated(
                      "/api/v1/admin/orders/export.csv",
                      "admin-paid-orders.csv"
                    );
                    message.success("已导出全站已支付订单");
                  } catch (e) {
                    message.error(e instanceof Error ? e.message : "导出失败");
                  }
                }
              });
            }}
          >
            导出已支付订单 CSV
          </Button>
        ]
      }}
    >
      <ProCard gutter={16} wrap>
        <ProCard colSpan={6} title="服务费率" bordered>
          <Typography.Title level={3} style={{ margin: 0 }}>
            {feePct}%
          </Typography.Title>
          <Typography.Text type="secondary">
            {cfg?.platformFeeRateBps ?? "-"} bps · 只读
          </Typography.Text>
        </ProCard>
        <ProCard colSpan={6} title="结算周期" bordered>
          <Typography.Title level={3} style={{ margin: 0 }}>
            {cfg?.settlementCycle ?? "-"}
          </Typography.Title>
          <Typography.Text type="secondary">WEEKLY / MONTHLY · 只读</Typography.Text>
        </ProCard>
        <ProCard colSpan={6} title="最低提现" bordered>
          <Typography.Title level={3} style={{ margin: 0 }}>
            ¥{withdrawYuan}
          </Typography.Title>
          <Typography.Text type="secondary">只读展示，无打款联调</Typography.Text>
        </ProCard>
        <ProCard colSpan={6} title="自动确认收货" bordered>
          <Typography.Title level={3} style={{ margin: 0 }}>
            {autoDays} 天
          </Typography.Title>
          <Typography.Text type="secondary">签收后 · I32 可配置</Typography.Text>
        </ProCard>
      </ProCard>
      <ProCard title="配置明细" style={{ marginTop: 16 }}>
        <ProDescriptions column={1} size="small">
          <ProDescriptions.Item label="说明">
            费率类只读；auto_confirm_receipt_days 可由上方按钮修改。
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
