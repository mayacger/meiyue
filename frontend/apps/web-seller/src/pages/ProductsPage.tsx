import { useEffect, useRef, useState } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import {
  ModalForm,
  PageContainer,
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable
} from "@ant-design/pro-components";
import { App, Button, Space, Tag } from "antd";
import { apiFetch } from "@meiyue/api";
import type { ProductSummary } from "@meiyue/types";

interface Category {
  id: number;
  name: string;
}

/** 素材库项（与 MediaAssetResponse 对齐） */
interface MediaAsset {
  id: number;
  assetType: string;
  url: string;
  moderationStatus: string;
}

/**
 * 商品管理 CRUD（I16 + I22 封面选用）
 * GET/POST /seller/products · PUT /:id · POST /:id/status
 * 封面：POST /seller/ai/manual-images 登记 URL → POST …/cover-asset 挂接
 */
export function ProductsPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();
  const [categories, setCategories] = useState<Category[]>([]);
  const [assets, setAssets] = useState<MediaAsset[]>([]);

  useEffect(() => {
    apiFetch<Category[]>("/api/v1/categories").then(setCategories).catch(() => undefined);
    apiFetch<MediaAsset[]>("/api/v1/seller/ai/assets")
      .then((list) =>
        setAssets(list.filter((a) => a.assetType === "IMAGE" && a.moderationStatus === "APPROVED"))
      )
      .catch(() => setAssets([]));
  }, []);

  const categoryOptions = categories.map((c) => ({ label: c.name, value: c.id }));
  const assetOptions = assets.map((a) => ({
    label: `#${a.id} ${a.url.slice(0, 48)}`,
    value: a.id
  }));

  const columns: ProColumns<ProductSummary>[] = [
    { title: "ID", dataIndex: "id", width: 72 },
    { title: "标题", dataIndex: "title", ellipsis: true },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      render: (_, r) => {
        const color =
          r.status === "ON_SALE" ? "success" : r.status === "OFF_SALE" ? "default" : "processing";
        return <Tag color={color}>{r.status}</Tag>;
      }
    },
    {
      title: "价格",
      width: 110,
      render: (_, r) => `¥${((r.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}`
    },
    {
      title: "库存",
      width: 80,
      render: (_, r) => r.skus[0]?.stockQty ?? "-"
    },
    {
      title: "操作",
      valueType: "option",
      width: 320,
      render: (_, r) => (
        <Space wrap>
          <ModalForm
            title={`编辑商品 #${r.id}`}
            trigger={<Button type="link">编辑</Button>}
            initialValues={{
              categoryId: undefined,
              title: r.title,
              subtitle: r.subtitle || "",
              skuCode: r.skus[0]?.skuCode || "",
              priceYuan: (r.skus[0]?.priceCents ?? 0) / 100,
              stock: r.skus[0]?.stockQty ?? 0,
              detailHtml: "",
              coverImageUrl: r.coverImageUrl || ""
            }}
            onFinish={async (values) => {
              try {
                await apiFetch(`/api/v1/seller/products/${r.id}`, {
                  method: "PUT",
                  json: {
                    categoryId: values.categoryId ?? null,
                    title: values.title,
                    subtitle: values.subtitle || "",
                    detailHtml: values.detailHtml || "",
                    coverImageUrl: values.coverImageUrl || null,
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
                message.success("已更新");
                actionRef.current?.reload();
                return true;
              } catch (err) {
                message.error(err instanceof Error ? err.message : "更新失败");
                return false;
              }
            }}
          >
            <ProFormSelect name="categoryId" label="类目" options={categoryOptions} />
            <ProFormText name="title" label="标题" rules={[{ required: true }]} />
            <ProFormText name="subtitle" label="副标题" />
            <ProFormText name="coverImageUrl" label="封面 URL（直写）" />
            <ProFormText name="skuCode" label="SKU 编码" rules={[{ required: true }]} />
            <ProFormDigit name="priceYuan" label="价格（元）" min={0.01} rules={[{ required: true }]} />
            <ProFormDigit name="stock" label="库存" min={0} rules={[{ required: true }]} />
            <ProFormTextArea name="detailHtml" label="详情 HTML" />
          </ModalForm>
          <ModalForm
            title={`选用封面素材 · 商品 #${r.id}`}
            trigger={<Button type="link">选用素材</Button>}
            onFinish={async (values) => {
              try {
                if (values.newUrl) {
                  const asset = await apiFetch<MediaAsset>("/api/v1/seller/ai/manual-images", {
                    method: "POST",
                    json: { url: values.newUrl }
                  });
                  await apiFetch(`/api/v1/seller/ai/products/${r.id}/cover-asset`, {
                    method: "POST",
                    json: { assetId: asset.id }
                  });
                } else if (values.assetId) {
                  await apiFetch(`/api/v1/seller/ai/products/${r.id}/cover-asset`, {
                    method: "POST",
                    json: { assetId: values.assetId }
                  });
                } else {
                  message.warning("请选择素材或填写 URL");
                  return false;
                }
                message.success("封面已挂接");
                const list = await apiFetch<MediaAsset[]>("/api/v1/seller/ai/assets");
                setAssets(
                  list.filter((a) => a.assetType === "IMAGE" && a.moderationStatus === "APPROVED")
                );
                actionRef.current?.reload();
                return true;
              } catch (err) {
                message.error(err instanceof Error ? err.message : "挂接失败");
                return false;
              }
            }}
          >
            <ProFormSelect name="assetId" label="已有素材" options={assetOptions} />
            <ProFormText name="newUrl" label="或登记新 URL" placeholder="https://…" />
          </ModalForm>
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
    <PageContainer header={{ title: "商品管理", subTitle: "SPU/SKU · CRUD · 上下架 · 封面素材" }}>
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
                    subtitle: values.subtitle || "",
                    detailHtml: values.detailHtml || "",
                    coverImageUrl: values.coverImageUrl || null,
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
              options={categoryOptions}
              rules={[{ required: true }]}
            />
            <ProFormText name="title" label="标题" rules={[{ required: true }]} />
            <ProFormText name="subtitle" label="副标题" />
            <ProFormText name="coverImageUrl" label="封面 URL" />
            <ProFormText name="skuCode" label="SKU 编码" rules={[{ required: true }]} />
            <ProFormDigit
              name="priceYuan"
              label="价格（元）"
              initialValue={99}
              min={0.01}
              rules={[{ required: true }]}
            />
            <ProFormDigit name="stock" label="库存" initialValue={10} min={0} rules={[{ required: true }]} />
            <ProFormTextArea name="detailHtml" label="详情 HTML" />
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
