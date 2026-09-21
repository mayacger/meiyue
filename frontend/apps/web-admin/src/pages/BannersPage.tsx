import { useRef } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import {
  ModalForm,
  PageContainer,
  ProFormDigit,
  ProFormSwitch,
  ProFormText,
  ProTable
} from "@ant-design/pro-components";
import { App, Button, Popconfirm, Space, Tag } from "antd";
import { apiFetch } from "@meiyue/api";

/**
 * 平台首页 Banner CRUD（I28）
 *
 * 入口：AdminLayout → /banners
 * API：GET/POST/PUT/DELETE /api/v1/admin/banners
 * 公开：GET /api/v1/banners（Buyer/Taro 首页）
 * 禁直播组件
 */

interface Banner {
  id: number;
  title: string;
  imageUrl: string;
  linkUrl: string | null;
  sortOrder: number;
  enabled: boolean;
  startAt: string | null;
  endAt: string | null;
}

export function BannersPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();

  const columns: ProColumns<Banner>[] = [
    { title: "ID", dataIndex: "id", width: 72 },
    { title: "标题", dataIndex: "title", ellipsis: true },
    {
      title: "图片",
      dataIndex: "imageUrl",
      ellipsis: true,
      render: (_, r) => (
        <a href={r.imageUrl} target="_blank" rel="noreferrer">
          预览
        </a>
      )
    },
    { title: "跳转", dataIndex: "linkUrl", ellipsis: true },
    { title: "排序", dataIndex: "sortOrder", width: 72 },
    {
      title: "启用",
      dataIndex: "enabled",
      width: 88,
      render: (_, r) => <Tag color={r.enabled ? "success" : "default"}>{r.enabled ? "是" : "否"}</Tag>
    },
    {
      title: "操作",
      valueType: "option",
      width: 200,
      render: (_, r) => (
        <Space>
          <ModalForm
            title={`编辑 Banner #${r.id}`}
            trigger={<Button type="link">编辑</Button>}
            initialValues={{
              title: r.title,
              imageUrl: r.imageUrl,
              linkUrl: r.linkUrl || "",
              sortOrder: r.sortOrder,
              enabled: r.enabled
            }}
            onFinish={async (values) => {
              try {
                await apiFetch(`/api/v1/admin/banners/${r.id}`, {
                  method: "PUT",
                  json: {
                    title: values.title,
                    imageUrl: values.imageUrl,
                    linkUrl: values.linkUrl || null,
                    sortOrder: Number(values.sortOrder),
                    enabled: Boolean(values.enabled)
                  }
                });
                message.success("已保存");
                actionRef.current?.reload();
                return true;
              } catch (e) {
                message.error(e instanceof Error ? e.message : "保存失败");
                return false;
              }
            }}
          >
            <ProFormText name="title" label="标题" rules={[{ required: true }]} />
            <ProFormText name="imageUrl" label="图片 URL" rules={[{ required: true }]} />
            <ProFormText name="linkUrl" label="跳转（站内路径或外链）" />
            <ProFormDigit name="sortOrder" label="排序（大者优先）" min={0} />
            <ProFormSwitch name="enabled" label="启用" />
          </ModalForm>
          <Popconfirm
            title="确认删除？"
            onConfirm={async () => {
              try {
                await apiFetch(`/api/v1/admin/banners/${r.id}`, { method: "DELETE" });
                message.success("已删除");
                actionRef.current?.reload();
              } catch (e) {
                message.error(e instanceof Error ? e.message : "删除失败");
              }
            }}
          >
            <Button type="link" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ];

  return (
    <PageContainer header={{ title: "首页 Banner", subTitle: "运营位 CRUD · 禁直播 · 买家/Taro 首页展示" }}>
      <ProTable<Banner>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        toolBarRender={() => [
          <ModalForm
            key="create"
            title="新建 Banner"
            trigger={<Button type="primary">新建</Button>}
            initialValues={{ sortOrder: 0, enabled: true }}
            onFinish={async (values) => {
              try {
                await apiFetch("/api/v1/admin/banners", {
                  method: "POST",
                  json: {
                    title: values.title,
                    imageUrl: values.imageUrl,
                    linkUrl: values.linkUrl || null,
                    sortOrder: Number(values.sortOrder ?? 0),
                    enabled: values.enabled !== false
                  }
                });
                message.success("已创建");
                actionRef.current?.reload();
                return true;
              } catch (e) {
                message.error(e instanceof Error ? e.message : "创建失败");
                return false;
              }
            }}
          >
            <ProFormText name="title" label="标题" rules={[{ required: true }]} />
            <ProFormText name="imageUrl" label="图片 URL" rules={[{ required: true }]} />
            <ProFormText name="linkUrl" label="跳转" placeholder="/products" />
            <ProFormDigit name="sortOrder" label="排序" min={0} />
            <ProFormSwitch name="enabled" label="启用" />
          </ModalForm>
        ]}
        request={async () => {
          const data = await apiFetch<Banner[]>("/api/v1/admin/banners");
          return { data, success: true, total: data.length };
        }}
      />
    </PageContainer>
  );
}
