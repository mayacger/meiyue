import { useEffect, useState } from "react";
import { PageContainer, ProCard, ProTable } from "@ant-design/pro-components";
import type { ProColumns } from "@ant-design/pro-components";
import { App, InputNumber, Space, Tag, Typography } from "antd";
import { apiFetch } from "@meiyue/api";

/**
 * 平台结算只读汇总（I28）
 *
 * 入口：AdminLayout → /settlements
 * API：GET /admin/settlements/periods · /admin/settlements/bills?tenantId=&periodKey=
 * 无真实分账打款
 */

interface Period {
  periodKey: string;
  status: string;
  amountCents: number;
  entryCount: number;
  tenantCount: number;
}

interface Bill {
  id: number;
  tenantId?: number;
  orderId: number;
  entryType: string;
  amountCents: number;
  status: string;
  periodKey: string;
  remark: string | null;
}

export function SettlementsPage() {
  const { message } = App.useApp();
  const [periods, setPeriods] = useState<Period[]>([]);
  const [bills, setBills] = useState<Bill[]>([]);
  const [periodKey, setPeriodKey] = useState<string | undefined>();
  const [tenantId, setTenantId] = useState<number | null>(null);

  async function loadPeriods() {
    setPeriods(await apiFetch<Period[]>("/api/v1/admin/settlements/periods"));
  }

  async function loadBills(pk?: string, tid?: number | null) {
    const q = new URLSearchParams();
    if (pk) q.set("periodKey", pk);
    if (tid != null) q.set("tenantId", String(tid));
    const qs = q.toString();
    setBills(await apiFetch<Bill[]>(`/api/v1/admin/settlements/bills${qs ? `?${qs}` : ""}`));
  }

  useEffect(() => {
    (async () => {
      try {
        await loadPeriods();
        await loadBills();
      } catch (e) {
        message.error(e instanceof Error ? e.message : "加载失败");
      }
    })();
  }, [message]);

  const periodCols: ProColumns<Period>[] = [
    { title: "周期", dataIndex: "periodKey" },
    {
      title: "状态",
      dataIndex: "status",
      render: (_, r) => <Tag>{r.status}</Tag>
    },
    {
      title: "金额",
      render: (_, r) => `¥${(r.amountCents / 100).toFixed(2)}`
    },
    { title: "笔数", dataIndex: "entryCount", width: 80 },
    { title: "商家数", dataIndex: "tenantCount", width: 88 }
  ];

  const billCols: ProColumns<Bill>[] = [
    { title: "ID", dataIndex: "id", width: 72 },
    { title: "租户", dataIndex: "tenantId", width: 88 },
    { title: "订单", dataIndex: "orderId", width: 88 },
    { title: "类型", dataIndex: "entryType", width: 100 },
    {
      title: "金额",
      render: (_, r) => `¥${(r.amountCents / 100).toFixed(2)}`
    },
    { title: "状态", dataIndex: "status", width: 100 },
    { title: "周期", dataIndex: "periodKey", width: 120 }
  ];

  return (
    <PageContainer header={{ title: "结算汇总", subTitle: "全站账本只读 · 无自动打款 · 无真实分账" }}>
      <ProCard title="周期汇总" style={{ marginBottom: 16 }}>
        <ProTable<Period>
          rowKey={(r) => `${r.periodKey}-${r.status}`}
          search={false}
          toolBarRender={false}
          pagination={false}
          dataSource={periods}
          columns={periodCols}
          onRow={(r) => ({
            onClick: () => {
              setPeriodKey(r.periodKey);
              loadBills(r.periodKey, tenantId).catch((e) =>
                message.error(e instanceof Error ? e.message : "加载失败")
              );
            },
            style: { cursor: "pointer" }
          })}
        />
        <Typography.Paragraph type="secondary" style={{ marginTop: 8 }}>
          点击行按周期筛选下方明细；当前筛选：{periodKey || "全部"}
        </Typography.Paragraph>
      </ProCard>
      <ProCard
        title="账单明细"
        extra={
          <Space>
            <span>租户 ID</span>
            <InputNumber
              min={1}
              value={tenantId ?? undefined}
              onChange={(v) => setTenantId(typeof v === "number" ? v : null)}
              placeholder="可选"
            />
            <a
              onClick={() =>
                loadBills(periodKey, tenantId).catch((e) =>
                  message.error(e instanceof Error ? e.message : "加载失败")
                )
              }
            >
              筛选
            </a>
            <a
              onClick={() => {
                setPeriodKey(undefined);
                setTenantId(null);
                loadBills().catch((e) => message.error(e instanceof Error ? e.message : "加载失败"));
              }}
            >
              重置
            </a>
          </Space>
        }
      >
        <ProTable<Bill>
          rowKey="id"
          search={false}
          toolBarRender={false}
          dataSource={bills}
          columns={billCols}
        />
      </ProCard>
    </PageContainer>
  );
}
