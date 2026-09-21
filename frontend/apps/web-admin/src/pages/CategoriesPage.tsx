import { useRef } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { PageContainer, ProTable } from "@ant-design/pro-components";
import { Tag } from "antd";
import { apiFetch } from "@meiyue/api";

interface Category {
  id: number;
  name: string;
  parentId?: number | null;
  sortOrder?: number;
}

/**
 * 平台统一类目（只读）
 * API：GET /api/v1/categories（公开；后台只读展示）
 * 可编辑类目 API 未开放，本页不提供写操作
 */
export function CategoriesPage() {
  const actionRef = useRef<ActionType>();

  const columns: ProColumns<Category>[] = [
    { title: "ID", dataIndex: "id", width: 80 },
    { title: "名称", dataIndex: "name" },
    {
      title: "父级",
      dataIndex: "parentId",
      render: (_, r) => (r.parentId ? r.parentId : <Tag>根类目</Tag>)
    },
    { title: "排序", dataIndex: "sortOrder", width: 90 }
  ];

  return (
    <PageContainer
      header={{
        title: "类目管理",
        subTitle: "平台统一类目 · 只读（写接口待后端开放）"
      }}
    >
      <ProTable<Category>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        toolBarRender={false}
        request={async () => {
          const data = await apiFetch<Category[]>("/api/v1/categories");
          return { data, success: true, total: data.length };
        }}
        pagination={{ pageSize: 20 }}
      />
    </PageContainer>
  );
}
