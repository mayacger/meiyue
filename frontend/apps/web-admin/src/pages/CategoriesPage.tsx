import { useRef, useState } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import {
  ModalForm,
  PageContainer,
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProTable
} from "@ant-design/pro-components";
import { App, Button, Popconfirm, Space, Tag } from "antd";
import { apiFetch } from "@meiyue/api";

/**
 * 平台类目管理（I18）
 * API：
 * - GET /admin/categories（含禁用）
 * - POST /admin/categories
 * - PUT /admin/categories/{id}
 * - POST /admin/categories/{id}/status
 *
 * 字段：id / parentId / name / sortOrder / status(ENABLED|DISABLED)
 */

interface Category {
  /** 主键 */
  id: number;
  /** 类目名称 */
  name: string;
  /** 父类目 ID，根为 null */
  parentId?: number | null;
  /** 排序，越小越前 */
  sortOrder?: number;
  /** ENABLED | DISABLED */
  status?: "ENABLED" | "DISABLED";
}

export function CategoriesPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();
  const [parentOptions, setParentOptions] = useState<{ label: string; value: number }[]>([]);

  async function loadParents() {
    const list = await apiFetch<Category[]>("/api/v1/admin/categories");
    setParentOptions(
      list
        .filter((c) => c.status !== "DISABLED")
        .map((c) => ({ label: `${c.name} (#${c.id})`, value: c.id }))
    );
  }

  const columns: ProColumns<Category>[] = [
    { title: "ID", dataIndex: "id", width: 80 },
    { title: "名称", dataIndex: "name" },
    {
      title: "父级",
      dataIndex: "parentId",
      render: (_, r) => (r.parentId ? r.parentId : <Tag>根类目</Tag>)
    },
    { title: "排序", dataIndex: "sortOrder", width: 90 },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      render: (_, r) => (
        <Tag color={r.status === "DISABLED" ? "default" : "green"}>{r.status ?? "ENABLED"}</Tag>
      )
    },
    {
      title: "操作",
      valueType: "option",
      width: 220,
      render: (_, r) => (
        <Space>
          <ModalForm
            title={`编辑类目 #${r.id}`}
            trigger={<a>编辑</a>}
            initialValues={{
              name: r.name,
              parentId: r.parentId ?? undefined,
              sortOrder: r.sortOrder ?? 0
            }}
            modalProps={{ destroyOnClose: true }}
            onOpenChange={(open) => {
              if (open) loadParents().catch(() => undefined);
            }}
            onFinish={async (values) => {
              try {
                await apiFetch(`/api/v1/admin/categories/${r.id}`, {
                  method: "PUT",
                  json: {
                    name: values.name,
                    parentId: values.parentId ?? null,
                    sortOrder: Number(values.sortOrder ?? 0)
                  }
                });
                message.success("已更新");
                actionRef.current?.reload();
                return true;
              } catch (err) {
                message.error(err instanceof Error ? err.message : "更新失败");
                return false;
              }
            }}
          >
            <ProFormText name="name" label="名称" rules={[{ required: true }]} />
            <ProFormSelect
              name="parentId"
              label="父类目"
              options={parentOptions.filter((o) => o.value !== r.id)}
              allowClear
              placeholder="空为根类目"
            />
            <ProFormDigit name="sortOrder" label="排序" min={0} />
          </ModalForm>
          <Popconfirm
            title={r.status === "DISABLED" ? "启用该类目？" : "禁用该类目？"}
            onConfirm={async () => {
              try {
                await apiFetch(`/api/v1/admin/categories/${r.id}/status`, {
                  method: "POST",
                  json: { status: r.status === "DISABLED" ? "ENABLED" : "DISABLED" }
                });
                message.success("状态已更新");
                actionRef.current?.reload();
              } catch (err) {
                message.error(err instanceof Error ? err.message : "操作失败");
              }
            }}
          >
            <a>{r.status === "DISABLED" ? "启用" : "禁用"}</a>
          </Popconfirm>
        </Space>
      )
    }
  ];

  return (
    <PageContainer
      header={{
        title: "类目管理",
        subTitle: "平台统一类目 · 创建 / 更新 / 启停"
      }}
    >
      <ProTable<Category>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        toolBarRender={() => [
          <ModalForm
            key="create"
            title="新建类目"
            trigger={<Button type="primary">新建类目</Button>}
            modalProps={{ destroyOnClose: true }}
            onOpenChange={(open) => {
              if (open) loadParents().catch(() => undefined);
            }}
            onFinish={async (values) => {
              try {
                await apiFetch("/api/v1/admin/categories", {
                  method: "POST",
                  json: {
                    name: values.name,
                    parentId: values.parentId ?? null,
                    sortOrder: Number(values.sortOrder ?? 0)
                  }
                });
                message.success("已创建");
                actionRef.current?.reload();
                return true;
              } catch (err) {
                message.error(err instanceof Error ? err.message : "创建失败");
                return false;
              }
            }}
          >
            <ProFormText name="name" label="名称" rules={[{ required: true }]} />
            <ProFormSelect
              name="parentId"
              label="父类目"
              options={parentOptions}
              allowClear
              placeholder="空为根类目"
            />
            <ProFormDigit name="sortOrder" label="排序" initialValue={0} min={0} />
          </ModalForm>
        ]}
        request={async () => {
          const data = await apiFetch<Category[]>("/api/v1/admin/categories");
          return { data, success: true, total: data.length };
        }}
        pagination={{ pageSize: 20 }}
      />
    </PageContainer>
  );
}
