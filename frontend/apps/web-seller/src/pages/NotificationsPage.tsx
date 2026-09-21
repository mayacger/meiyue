import { useRef, useState } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { PageContainer, ProTable } from "@ant-design/pro-components";
import { App, Button, Segmented, Tag } from "antd";
import { apiFetch } from "@meiyue/api";
import type { NotificationItem } from "@meiyue/types";

/**
 * 商家消息中心（I19 + I25 已读未读）
 * API：GET /notifications · POST /{id}/read · read-all
 */
export function NotificationsPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();
  const [filter, setFilter] = useState<"all" | "unread">("all");
  const [unread, setUnread] = useState(0);

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
    <PageContainer
      header={{
        title: "消息中心",
        subTitle: `未读 ${unread} · 已读/未读筛选`
      }}
    >
      <ProTable<NotificationItem>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        params={{ filter }}
        toolBarRender={() => [
          <Segmented
            key="filter"
            value={filter}
            onChange={(v) => setFilter(v as "all" | "unread")}
            options={[
              { label: "全部", value: "all" },
              { label: "未读", value: "unread" }
            ]}
          />,
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
          const [items, count] = await Promise.all([
            apiFetch<NotificationItem[]>("/api/v1/notifications"),
            apiFetch<{ unread: number }>("/api/v1/notifications/unread-count")
          ]);
          setUnread(count.unread ?? 0);
          const data = filter === "unread" ? items.filter((n) => !n.read) : items;
          return { data, success: true, total: data.length };
        }}
      />
    </PageContainer>
  );
}
