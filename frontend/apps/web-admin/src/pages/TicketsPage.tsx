import { useRef } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import {
  ModalForm,
  PageContainer,
  ProFormTextArea,
  ProTable
} from "@ant-design/pro-components";
import { App, Button, Space, Tag } from "antd";
import { apiFetch } from "@meiyue/api";

/**
 * 平台客服工单（I21）
 * GET /admin/tickets · POST reply / close
 */
interface Ticket {
  id: number;
  ticketNo: string;
  userId: number;
  tenantId: number | null;
  subject: string;
  body: string;
  category: string;
  status: string;
  adminReply: string | null;
  sellerReply: string | null;
  createdAt: string;
}

export function TicketsPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();

  const columns: ProColumns<Ticket>[] = [
    { title: "工单号", dataIndex: "ticketNo", copyable: true },
    { title: "用户", dataIndex: "userId", width: 80 },
    { title: "租户", dataIndex: "tenantId", width: 80, render: (_, r) => r.tenantId ?? "平台" },
    { title: "标题", dataIndex: "subject", ellipsis: true },
    { title: "分类", dataIndex: "category", width: 100 },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      render: (_, r) => <Tag>{r.status}</Tag>
    },
    { title: "时间", dataIndex: "createdAt", valueType: "dateTime", width: 180 },
    {
      title: "操作",
      valueType: "option",
      width: 200,
      render: (_, r) => (
        <Space>
          <ModalForm
            title={`工单 ${r.ticketNo}`}
            trigger={<Button type="link">回复</Button>}
            initialValues={{ reply: r.adminReply || "" }}
            onFinish={async (values) => {
              try {
                await apiFetch(`/api/v1/admin/tickets/${r.id}/reply`, {
                  method: "POST",
                  json: { reply: values.reply }
                });
                message.success("已回复");
                actionRef.current?.reload();
                return true;
              } catch (err) {
                message.error(err instanceof Error ? err.message : "失败");
                return false;
              }
            }}
          >
            <p>{r.body}</p>
            {r.sellerReply ? <p>商家回复：{r.sellerReply}</p> : null}
            <ProFormTextArea name="reply" label="平台回复" rules={[{ required: true }]} />
          </ModalForm>
          {r.status !== "CLOSED" ? (
            <Button
              type="link"
              onClick={async () => {
                try {
                  await apiFetch(`/api/v1/admin/tickets/${r.id}/close`, { method: "POST" });
                  message.success("已关闭");
                  actionRef.current?.reload();
                } catch (err) {
                  message.error(err instanceof Error ? err.message : "失败");
                }
              }}
            >
              关闭
            </Button>
          ) : null}
        </Space>
      )
    }
  ];

  return (
    <PageContainer header={{ title: "客服工单", subTitle: "平台处理 · 非 IM" }}>
      <ProTable<Ticket>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        request={async () => {
          const data = await apiFetch<Ticket[]>("/api/v1/admin/tickets");
          return { data, success: true, total: data.length };
        }}
      />
    </PageContainer>
  );
}
