import { useRef } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { PageContainer, ProTable } from "@ant-design/pro-components";
import { App, Button, Tag } from "antd";
import { apiFetch } from "@meiyue/api";
import type { NotificationItem } from "@meiyue/types";

/**
 * 商家站内通知箱（I19）
 * API：GET /notifications · POST /{id}/read · read-all
 * 字段：id / title / body / category / read / createdAt
 */
export function NotificationsPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();

  const columns: ProColumns<NotificationItem>[] = [
    { title: "标题", dataIndex: "title" },
    {
      title: "分类",
      dataIndex: "category",
      width: 100,
      render: (_, r) => <Tag>{r.category}</Tag>
    },
    { title: "正文", dataIndex: "body", ellipsis: true },
    {
      title: "状态",
      dataIndex: "read",
      width: 90,
      render: (_, r) => (r.read ? <Tag>已读</Tag> : <Tag color="green">未读</Tag>)
    },
    { title: "时间", dataIndex: "createdAt", valueType: "dateTime", width: 180 },
    {
      title: "操作",
      valueType: "option",
      width: 100,
      render: (_, r) =>
        r.read ? (
          "-"
        ) : (
          <a
            onClick={async () => {
              try {
                await apiFetch(`/api/v1/notifications/${r.id}/read`, { method: "POST" });
                message.success("已标已读");
                actionRef.current?.reload();
              } catch (err) {
                message.error(err instanceof Error ? err.message : "失败");
              }
            }}
          >
            标已读
          </a>
        )
    }
  ];

  return (
    <PageContainer header={{ title: "站内通知", subTitle: "商家通知箱 · 已读/未读" }}>
      <ProTable<NotificationItem>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        toolBarRender={() => [
          <Button
            key="all"
            onClick={async () => {
              try {
                await apiFetch("/api/v1/notifications/read-all", { method: "POST" });
                message.success("已全部标已读");
                actionRef.current?.reload();
              } catch (err) {
                message.error(err instanceof Error ? err.message : "失败");
              }
            }}
          >
            全部已读
          </Button>
        ]}
        request={async () => {
          const data = await apiFetch<NotificationItem[]>("/api/v1/notifications");
          return { data, success: true, total: data.length };
        }}
        pagination={{ pageSize: 20 }}
      />
    </PageContainer>
  );
}
