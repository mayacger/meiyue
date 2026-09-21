import { useEffect, useRef, useState } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import {
  ModalForm,
  PageContainer,
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProTable
} from "@ant-design/pro-components";
import { App, Button, Space, Tag } from "antd";
import { apiFetch } from "@meiyue/api";
import type { ProductSummary } from "@meiyue/types";

interface Category {
  id: number;
  name: string;
}

/**
 * 商品管理 ProTable
 * API：GET/POST /seller/products；POST /:id/status
 * 字段：title / categoryId / skuCode / priceCents / stockQty
 */
export function ProductsPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();
  const [categories, setCategories] = useState<Category[]>([]);

  useEffect(() => {
    apiFetch<Category[]>("/api/v1/categories").then(setCategories).catch(() => undefined);
  }, []);

  const columns: ProColumns<ProductSummary>[] = [
    { title: "ID", dataIndex: "id", width: 72 },
    { title: "标题", dataIndex: "title" },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      render: (_, r) => <Tag>{r.status}</Tag>
    },
    {
      title: "价格",
      render: (_, r) => `¥${((r.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}`
    },
    {
      title: "库存",
      render: (_, r) => r.skus[0]?.stockQty ?? "-"
    },
    {
      title: "操作",
      valueType: "option",
      render: (_, r) => (
        <Space>
          <Button
            type="link"
            onClick={async () => {
              await apiFetch(`/api/v1/seller/products/${r.id}/status`, {
                method: "POST",
                json: { status: "ON_SALE" }
              });
              message.success("已上架");
              actionRef.current?.reload();
            }}
          >
            上架
          </Button>
          <Button
            type="link"
            onClick={async () => {
              await apiFetch(`/api/v1/seller/products/${r.id}/status`, {
                method: "POST",
                json: { status: "OFF_SALE" }
              });
              message.success("已下架");
              actionRef.current?.reload();
            }}
          >
            下架
          </Button>
        </Space>
      )
    }
  ];

  return (
    <PageContainer header={{ title: "商品管理", subTitle: "SPU/SKU · 上下架" }}>
      <ProTable<ProductSummary>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        toolBarRender={() => [
          <ModalForm
            key="create"
            title="创建草稿商品"
            trigger={<Button type="primary">新建商品</Button>}
            onFinish={async (values) => {
              try {
                await apiFetch("/api/v1/seller/products", {
                  method: "POST",
                  json: {
                    categoryId: values.categoryId,
                    title: values.title,
                    subtitle: "",
                    detailHtml: "",
                    skus: [
                      {
                        skuCode: values.skuCode,
                        specText: "默认",
                        priceCents: Math.round(Number(values.priceYuan) * 100),
                        stockQty: Number(values.stock)
                      }
                    ]
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
            <ProFormSelect
              name="categoryId"
              label="类目"
              options={categories.map((c) => ({ label: c.name, value: c.id }))}
              rules={[{ required: true }]}
            />
            <ProFormText name="title" label="标题" rules={[{ required: true }]} />
            <ProFormText name="skuCode" label="SKU 编码" rules={[{ required: true }]} />
            <ProFormDigit name="priceYuan" label="价格（元）" initialValue={99} min={0.01} rules={[{ required: true }]} />
            <ProFormDigit name="stock" label="库存" initialValue={10} min={0} rules={[{ required: true }]} />
          </ModalForm>
        ]}
        request={async () => {
          const data = await apiFetch<ProductSummary[]>("/api/v1/seller/products");
          return { data, success: true, total: data.length };
        }}
      />
    </PageContainer>
  );
}
