import { useEffect, useRef, useState, type Key } from "react";
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
import { App, Button, Space, Tabs, Tag } from "antd";
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
 * 商品管理 CRUD（I16 + I22 封面选用 + I25 批量上下架 + I27 草稿箱）
 *
 * 入口：SellerLayout → /products
 * API：
 *   - GET/POST /seller/products · PUT /:id · POST /:id/status · POST /batch-status
 *   - GET /seller/products/drafts（草稿箱 Tab）
 * 封面：POST /seller/ai/manual-images → POST …/cover-asset
 */
export function ProductsPage() {
  const actionRef = useRef<ActionType>();
  const { message, modal } = App.useApp();
  const [categories, setCategories] = useState<Category[]>([]);
  const [assets, setAssets] = useState<MediaAsset[]>([]);
  /** I25：批量上下架选中行 */
  const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
  /** I27/I36：全部 | 草稿箱 | 回收站 */
  const [tab, setTab] = useState<"all" | "drafts" | "deleted">("all");

  useEffect(() => {
    apiFetch<Category[]>("/api/v1/categories").then(setCategories).catch(() => undefined);
    apiFetch<MediaAsset[]>("/api/v1/seller/ai/assets")
      .then((list) =>
        setAssets(list.filter((a) => a.assetType === "IMAGE" && a.moderationStatus === "APPROVED"))
      )
      .catch(() => setAssets([]));
  }, []);

  useEffect(() => {
    setSelectedRowKeys([]);
    actionRef.current?.reload();
  }, [tab]);

  async function batchStatus(status: "ON_SALE" | "OFF_SALE") {
    if (selectedRowKeys.length === 0) {
      message.warning("请先勾选商品");
      return;
    }
    try {
      await apiFetch("/api/v1/seller/products/batch-status", {
        method: "POST",
        json: { productIds: selectedRowKeys.map(Number), status }
      });
      message.success(status === "ON_SALE" ? "已批量上架" : "已批量下架");
      setSelectedRowKeys([]);
      actionRef.current?.reload();
    } catch (err) {
      message.error(err instanceof Error ? err.message : "批量操作失败");
    }
  }

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
      render: (_, r) =>
        tab === "deleted" ? (
          <Button
            type="link"
            onClick={async () => {
              try {
                await apiFetch(`/api/v1/seller/products/${r.id}/restore`, { method: "POST" });
                message.success("已恢复（仍为下架，请手动上架）");
                actionRef.current?.reload();
              } catch (err) {
                message.error(err instanceof Error ? err.message : "恢复失败");
              }
            }}
          >
            恢复
          </Button>
        ) : (
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
              coverImageUrl: r.coverImageUrl || "",
              galleryUrls: (r.galleryImageUrls || []).join("\n"),
              promoVideoUrl: r.promoVideoUrl || ""
            }}
            onFinish={async (values) => {
              try {
                const galleryImageUrls = String(values.galleryUrls || "")
                  .split(/[\n,]+/)
                  .map((s: string) => s.trim())
                  .filter(Boolean)
                  .slice(0, 20);
                await apiFetch(`/api/v1/seller/products/${r.id}`, {
                  method: "PUT",
                  json: {
                    categoryId: values.categoryId ?? null,
                    title: values.title,
                    subtitle: values.subtitle || "",
                    detailHtml: values.detailHtml || "",
                    coverImageUrl: values.coverImageUrl || null,
                    galleryImageUrls,
                    promoVideoUrl: values.promoVideoUrl || null,
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
            <ProFormTextArea
              name="galleryUrls"
              label="图集 URL（每行一个）"
              fieldProps={{ rows: 4, placeholder: "https://…\nhttps://…" }}
              extra="I34：最多 20 张；与封面一起在买家详情展示"
            />
            <ProFormText
              name="promoVideoUrl"
              label="推广视频 URL"
              placeholder="非直播 · mp4/webm 直链"
            />
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
          <Button
            type="link"
            danger
            onClick={() => {
              modal.confirm({
                title: `移入回收站 #${r.id}？`,
                content: "软删后默认列表不可见，可在回收站恢复。",
                okText: "移入回收站",
                onOk: async () => {
                  try {
                    await apiFetch(`/api/v1/seller/products/${r.id}`, { method: "DELETE" });
                    message.success("已移入回收站");
                    actionRef.current?.reload();
                  } catch (err) {
                    message.error(err instanceof Error ? err.message : "删除失败");
                  }
                }
              });
            }}
          >
            删除
          </Button>
        </Space>
        )
    }
  ];

  return (
    <PageContainer header={{ title: "商品管理", subTitle: "SPU/SKU · 草稿箱 · 回收站 · 批量上下架" }}>
      <Tabs
        activeKey={tab}
        onChange={(k) => setTab(k as "all" | "drafts" | "deleted")}
        items={[
          { key: "all", label: "全部商品" },
          { key: "drafts", label: "草稿箱" },
          { key: "deleted", label: "回收站" }
        ]}
        style={{ marginBottom: 8 }}
      />
      <ProTable<ProductSummary>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        rowSelection={
          tab === "deleted"
            ? false
            : {
                selectedRowKeys,
                onChange: setSelectedRowKeys
              }
        }
        toolBarRender={() =>
          tab === "deleted"
            ? []
            : [
          <Button key="on" disabled={!selectedRowKeys.length} onClick={() => batchStatus("ON_SALE")}>
            批量上架
          </Button>,
          <Button key="off" disabled={!selectedRowKeys.length} onClick={() => batchStatus("OFF_SALE")}>
            批量下架
          </Button>,
          <ModalForm
            key="create"
            title="创建草稿商品"
            trigger={<Button type="primary">新建商品</Button>}
            onFinish={async (values) => {
              try {
                // I34：创建时即可写入图集 / 推广视频（每行一个 URL，最多 20）
                const galleryImageUrls = String(values.galleryUrls || "")
                  .split(/[\n,]+/)
                  .map((s: string) => s.trim())
                  .filter(Boolean)
                  .slice(0, 20);
                await apiFetch("/api/v1/seller/products", {
                  method: "POST",
                  json: {
                    categoryId: values.categoryId,
                    title: values.title,
                    subtitle: values.subtitle || "",
                    detailHtml: values.detailHtml || "",
                    coverImageUrl: values.coverImageUrl || null,
                    galleryImageUrls,
                    promoVideoUrl: values.promoVideoUrl || null,
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
                message.success("已创建草稿");
                setTab("drafts");
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
            <ProFormTextArea
              name="galleryUrls"
              label="图集 URL（每行一个）"
              fieldProps={{ rows: 3, placeholder: "https://…\nhttps://…" }}
              extra="I34：新建草稿即可维护图集"
            />
            <ProFormText
              name="promoVideoUrl"
              label="推广视频 URL"
              placeholder="非直播 · mp4/webm 直链"
            />
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
            ]
        }
        params={{ tab }}
        request={async () => {
          const path =
            tab === "drafts"
              ? "/api/v1/seller/products/drafts"
              : tab === "deleted"
                ? "/api/v1/seller/products/deleted"
                : "/api/v1/seller/products";
          const data = await apiFetch<ProductSummary[]>(path);
          return { data, success: true, total: data.length };
        }}
      />
    </PageContainer>
  );
}
