import { useEffect, useState } from "react";
import { PageContainer, ProCard, ProTable } from "@ant-design/pro-components";
import type { ProColumns } from "@ant-design/pro-components";
import { App, Tag } from "antd";
import { apiFetch } from "@meiyue/api";

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
    <PageContainer header={{ title: "结算账本", subTitle: "MVP 记账结算，不自动打款" }}>
      <ProCard title="周期汇总" style={{ marginBottom: 16 }}>
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
