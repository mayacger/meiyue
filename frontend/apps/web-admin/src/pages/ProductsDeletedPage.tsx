import { useRef } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { PageContainer, ProTable } from "@ant-design/pro-components";
import { App, Button, Tag } from "antd";
import { apiFetch } from "@meiyue/api";
import type { ProductSummary } from "@meiyue/types";

/**
 * I36：平台商品回收站治理
 * 入口：AdminLayout → /products-deleted
 * API：GET /admin/products/deleted · POST /admin/products/{id}/restore
 */
export function ProductsDeletedPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();

  const columns: ProColumns<ProductSummary>[] = [
    { title: "ID", dataIndex: "id", width: 72 },
    { title: "租户", dataIndex: "tenantId", width: 80 },
    { title: "标题", dataIndex: "title", ellipsis: true },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      render: (_, r) => <Tag>{r.status}</Tag>
    },
    {
      title: "软删时间",
      dataIndex: "deletedAt",
      width: 200,
      render: (_, r) => r.deletedAt || "—"
    },
    {
      title: "操作",
      valueType: "option",
      width: 120,
      render: (_, r) => [
        <Button
          key="restore"
          type="link"
          onClick={async () => {
            try {
              await apiFetch(`/api/v1/admin/products/${r.id}/restore`, { method: "POST" });
              message.success("已代恢复");
              actionRef.current?.reload();
            } catch (err) {
              message.error(err instanceof Error ? err.message : "恢复失败");
            }
          }}
        >
          恢复
        </Button>
      ]
    }
  ];

  return (
    <PageContainer header={{ title: "商品回收站", subTitle: "平台治理 · 代恢复 · 无硬删" }}>
      <ProTable<ProductSummary>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        request={async () => {
          const data = await apiFetch<ProductSummary[]>("/api/v1/admin/products/deleted");
          return { data, success: true, total: data.length };
        }}
      />
    </PageContainer>
  );
}
