import { useRef } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { PageContainer, ProTable } from "@ant-design/pro-components";
import { App, Button, Space, Tag } from "antd";
import { apiFetch } from "@meiyue/api";

interface Aftersale {
  id: number;
  aftersaleNo: string;
  orderId: number;
  type: string;
  status: string;
  reason: string;
  refundCents: number;
  reverseShipmentId: number | null;
  evidenceImageUrls?: string[];
}

/**
 * 售后审核（I6 + I29 凭证图）
 * approve / reject / confirm-return
 */
export function AftersalesPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();

  const columns: ProColumns<Aftersale>[] = [
    { title: "单号", dataIndex: "aftersaleNo" },
    { title: "订单", dataIndex: "orderId", width: 80 },
    { title: "类型", dataIndex: "type" },
    {
      title: "状态",
      dataIndex: "status",
      render: (_, r) => <Tag>{r.status}</Tag>
    },
    { title: "原因", dataIndex: "reason", ellipsis: true },
    {
      title: "凭证",
      width: 120,
      render: (_, r) =>
        r.evidenceImageUrls && r.evidenceImageUrls.length > 0 ? (
          <Space wrap>
            {r.evidenceImageUrls.slice(0, 3).map((u) => (
              <a key={u} href={u} target="_blank" rel="noreferrer">
                图
              </a>
            ))}
          </Space>
        ) : (
          "—"
        )
    },
    {
      title: "退款",
      render: (_, r) => `¥${(r.refundCents / 100).toFixed(2)}`
    },
    {
      title: "操作",
      valueType: "option",
      render: (_, r) => (
        <Space>
          {(r.status === "REVIEWING" || r.status === "APPLIED") && (
            <>
              <Button
                type="link"
                onClick={async () => {
                  await apiFetch(`/api/v1/seller/aftersales/${r.id}/approve`, {
                    method: "POST",
                    json: { reviewNote: "商家同意" }
                  });
                  message.success("已同意");
                  actionRef.current?.reload();
                }}
              >
                同意
              </Button>
              <Button
                type="link"
                danger
                onClick={async () => {
                  await apiFetch(`/api/v1/seller/aftersales/${r.id}/reject`, {
                    method: "POST",
                    json: { reviewNote: "商家拒绝" }
                  });
                  message.success("已拒绝");
                  actionRef.current?.reload();
                }}
              >
                拒绝
              </Button>
            </>
          )}
          {r.type === "RETURN_REFUND" && r.status === "APPROVED" && r.reverseShipmentId ? (
            <Button
              type="link"
              onClick={async () => {
                await apiFetch(`/api/v1/seller/aftersales/${r.id}/confirm-return`, {
                  method: "POST"
                });
                message.success("已确认收货");
                actionRef.current?.reload();
              }}
            >
              确认退货签收
            </Button>
          ) : null}
          {r.type === "RETURN_REFUND" && r.status === "APPROVED" && !r.reverseShipmentId ? (
            <Tag color="orange">待买家填运单</Tag>
          ) : null}
        </Space>
      )
    }
  ];

  return (
    <PageContainer header={{ title: "售后审核", subTitle: "凭证图可点开 · 48h 超时自动同意" }}>
      <ProTable<Aftersale>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        request={async () => {
          const data = await apiFetch<Aftersale[]>("/api/v1/seller/aftersales");
          return { data, success: true, total: data.length };
        }}
      />
    </PageContainer>
  );
}
