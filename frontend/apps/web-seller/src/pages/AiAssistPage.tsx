import { useEffect, useState } from "react";
import {
  PageContainer,
  ProCard,
  ProForm,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  ProTable
} from "@ant-design/pro-components";
import type { ProColumns } from "@ant-design/pro-components";
import { App, Tag } from "antd";
import { apiFetch } from "@meiyue/api";
import type { ProductSummary } from "@meiyue/types";

interface VideoTask {
  id: number;
  status: string;
  prompt: string;
  resultUrl: string | null;
  productId: number | null;
}

/** 与 MediaAssetResponse 对齐：assetType / moderationStatus / url */
interface AiAsset {
  id: number;
  assetType: string;
  moderationStatus: string;
  prompt?: string;
  url?: string | null;
}

/**
 * AI 素材入口（I16 + I22 URL 登记）
 * - 出图 / 详情 / 推广视频 MOCK
 * - POST /seller/ai/manual-images 登记 URL → media_assets
 * - 商品页「选用素材」挂封面
 */
export function AiAssistPage() {
  const { message } = App.useApp();
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [videos, setVideos] = useState<VideoTask[]>([]);
  const [assets, setAssets] = useState<AiAsset[]>([]);

  async function reload() {
    setProducts(await apiFetch<ProductSummary[]>("/api/v1/seller/products"));
    setVideos(await apiFetch<VideoTask[]>("/api/v1/seller/ai/videos"));
    try {
      setAssets(await apiFetch<AiAsset[]>("/api/v1/seller/ai/assets"));
    } catch {
      setAssets([]);
    }
  }

  useEffect(() => {
    reload().catch((e) => message.error(e instanceof Error ? e.message : "加载失败"));
  }, [message]);

  const productOptions = products.map((p) => ({ label: `#${p.id} ${p.title}`, value: p.id }));

  const videoCols: ProColumns<VideoTask>[] = [
    { title: "ID", dataIndex: "id", width: 72 },
    { title: "提示词", dataIndex: "prompt", ellipsis: true },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      render: (_, r) => <Tag>{r.status}</Tag>
    },
    { title: "商品", dataIndex: "productId", width: 90 },
    { title: "结果", dataIndex: "resultUrl", ellipsis: true }
  ];

  const assetCols: ProColumns<AiAsset>[] = [
    { title: "ID", dataIndex: "id", width: 72 },
    { title: "类型", dataIndex: "assetType", width: 100 },
    {
      title: "状态",
      dataIndex: "moderationStatus",
      width: 100,
      render: (_, r) => <Tag>{r.moderationStatus}</Tag>
    },
    { title: "提示词", dataIndex: "prompt", ellipsis: true },
    { title: "URL", dataIndex: "url", ellipsis: true }
  ];

  return (
    <PageContainer header={{ title: "AI 素材", subTitle: "出图 / 详情 / 视频 MOCK · URL 登记 · 非直播" }}>
      <ProCard gutter={16} wrap>
        <ProCard colSpan={8} title="AI 出图" bordered>
          <ProForm
            onFinish={async (values) => {
              try {
                await apiFetch("/api/v1/seller/ai/images", {
                  method: "POST",
                  json: { prompt: values.prompt }
                });
                message.success("出图任务已提交");
                await reload();
                return true;
              } catch (err) {
                message.error(err instanceof Error ? err.message : "提交失败");
                return false;
              }
            }}
          >
            <ProFormTextArea name="prompt" label="提示词" rules={[{ required: true }]} />
          </ProForm>
        </ProCard>
        <ProCard colSpan={8} title="AI 详情文案" bordered>
          <ProForm
            onFinish={async (values) => {
              try {
                await apiFetch("/api/v1/seller/ai/details", {
                  method: "POST",
                  json: {
                    title: values.title,
                    hints: values.hints || ""
                  }
                });
                message.success("详情任务已提交");
                await reload();
                return true;
              } catch (err) {
                message.error(err instanceof Error ? err.message : "提交失败");
                return false;
              }
            }}
          >
            <ProFormText name="title" label="商品标题" rules={[{ required: true }]} />
            <ProFormTextArea name="hints" label="卖点提示" />
          </ProForm>
        </ProCard>
        <ProCard colSpan={8} title="URL 登记（占位）" bordered>
          <ProForm
            onFinish={async (values) => {
              try {
                await apiFetch("/api/v1/seller/ai/manual-images", {
                  method: "POST",
                  json: { url: values.url }
                });
                message.success("已登记到素材库（非强制 OSS）");
                await reload();
                return true;
              } catch (err) {
                message.error(err instanceof Error ? err.message : "登记失败");
                return false;
              }
            }}
          >
            <ProFormText
              name="url"
              label="图片 URL"
              placeholder="https://…"
              rules={[{ required: true }]}
            />
          </ProForm>
        </ProCard>
        <ProCard colSpan={8} title="推广视频（非直播）" bordered>
          <ProForm
            onFinish={async (values) => {
              try {
                await apiFetch("/api/v1/seller/ai/videos", {
                  method: "POST",
                  json: { prompt: values.prompt, productId: values.productId || null }
                });
                message.success("视频任务已提交（MOCK）");
                await reload();
                return true;
              } catch (err) {
                message.error(err instanceof Error ? err.message : "提交失败");
                return false;
              }
            }}
          >
            <ProFormTextArea
              name="prompt"
              label="提示词"
              initialValue="商品展示短视频"
              rules={[{ required: true }]}
            />
            <ProFormSelect name="productId" label="关联商品" options={productOptions} allowClear />
          </ProForm>
        </ProCard>
      </ProCard>

      <ProCard title="视频任务" style={{ marginTop: 16 }}>
        <ProTable<VideoTask>
          rowKey="id"
          search={false}
          toolBarRender={false}
          dataSource={videos}
          columns={videoCols}
          pagination={{ pageSize: 5 }}
        />
      </ProCard>
      <ProCard title="素材资产" style={{ marginTop: 16 }}>
        <ProTable<AiAsset>
          rowKey="id"
          search={false}
          toolBarRender={false}
          dataSource={assets}
          columns={assetCols}
          locale={{ emptyText: "暂无素材" }}
          pagination={{ pageSize: 5 }}
        />
      </ProCard>
    </PageContainer>
  );
}
