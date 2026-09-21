import { useEffect, useState } from "react";
import { PageContainer, ProCard, ProTable } from "@ant-design/pro-components";
import type { ProColumns } from "@ant-design/pro-components";
import { App, Tag, Typography } from "antd";
import { apiFetch } from "@meiyue/api";
import type { PlatformConfigView } from "@meiyue/types";

interface Bill {
  id: number;
  orderId: number;
  entryType: string;
  amountCents: number;
  status: string;
  periodKey: string;
  remark: string | null;
}

interface Period {
  periodKey: string;
  status: string;
  amountCents: number;
  entryCount: number;
}

/** I23：费率只读提示（公开 /platform/config） */
function FeeHint() {
  const [text, setText] = useState("费率加载中…");
  useEffect(() => {
    apiFetch<PlatformConfigView>("/api/v1/platform/config")
      .then((c) => {
        const pct = (Number(c.platformFeeRateBps) / 100).toFixed(2);
        setText(`平台费率 ${pct}% · ${c.settlementCycle} · 只读`);
      })
      .catch(() => setText("费率暂不可用"));
  }, []);
  return <Typography.Text type="secondary">{text}</Typography.Text>;
}

/** 结算账本：周期汇总 + 明细（不自动打款） */
export function SettlementsPage() {
  const { message } = App.useApp();
  const [periods, setPeriods] = useState<Period[]>([]);
  const [bills, setBills] = useState<Bill[]>([]);

  useEffect(() => {
    (async () => {
      try {
        setPeriods(await apiFetch<Period[]>("/api/v1/seller/settlements/periods"));
        setBills(await apiFetch<Bill[]>("/api/v1/seller/settlements/bills"));
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
    { title: "笔数", dataIndex: "entryCount" }
  ];

  const billCols: ProColumns<Bill>[] = [
    { title: "ID", dataIndex: "id", width: 72 },
    { title: "订单", dataIndex: "orderId" },
    { title: "类型", dataIndex: "entryType" },
    {
      title: "金额",
      render: (_, r) => `¥${(r.amountCents / 100).toFixed(2)}`
    },
    { title: "状态", dataIndex: "status" },
    { title: "周期", dataIndex: "periodKey" }
  ];

  return (
    <PageContainer header={{ title: "结算账本", subTitle: "MVP 记账结算，不自动打款 · 费率只读见平台配置" }}>
      <ProCard title="周期汇总" style={{ marginBottom: 16 }} extra={<FeeHint />}>
        <ProTable<Period>
          rowKey={(r) => `${r.periodKey}-${r.status}`}
          search={false}
          toolBarRender={false}
          pagination={false}
          dataSource={periods}
          columns={periodCols}
        />
      </ProCard>
      <ProCard title="账单明细">
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
