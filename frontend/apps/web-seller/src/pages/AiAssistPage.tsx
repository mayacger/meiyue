import { useEffect, useState } from "react";
import {
  PageContainer,
  ProForm,
  ProFormSelect,
  ProFormTextArea
} from "@ant-design/pro-components";
import { App, Card, List, Tag } from "antd";
import { apiFetch } from "@meiyue/api";
import type { ProductSummary } from "@meiyue/types";

interface VideoTask {
  id: number;
  status: string;
  prompt: string;
  resultUrl: string | null;
  productId: number | null;
}

interface AiAsset {
  id: number;
  type: string;
  status: string;
  prompt?: string;
}

/**
 * AI 素材入口（图/详情/推广视频 MOCK）
 * 非直播；主交易不依赖 AI
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

  return (
    <PageContainer header={{ title: "AI 素材", subTitle: "出图/详情/推广视频 MOCK · 非直播" }}>
      <Card title="提交推广视频任务" style={{ marginBottom: 16 }}>
        <ProForm
          onFinish={async (values) => {
            try {
              await apiFetch("/api/v1/seller/ai/videos", {
                method: "POST",
                json: {
                  prompt: values.prompt,
                  productId: values.productId || null
                }
              });
              message.success("任务已提交（MOCK 异步）");
              await reload();
              return true;
            } catch (err) {
              message.error(err instanceof Error ? err.message : "提交失败");
              return false;
            }
          }}
        >
          <ProFormTextArea name="prompt" label="提示词" initialValue="商品展示短视频" rules={[{ required: true }]} />
          <ProFormSelect
            name="productId"
            label="关联商品"
            options={products.map((p) => ({ label: `#${p.id} ${p.title}`, value: p.id }))}
            allowClear
          />
        </ProForm>
      </Card>
      <Card title="视频任务" style={{ marginBottom: 16 }}>
        <List
          dataSource={videos}
          renderItem={(v) => (
            <List.Item>
              <List.Item.Meta
                title={
                  <>
                    任务#{v.id} <Tag>{v.status}</Tag>
                  </>
                }
                description={`${v.prompt}${v.resultUrl ? ` → ${v.resultUrl}` : ""}`}
              />
            </List.Item>
          )}
        />
      </Card>
      <Card title="素材资产">
        <List
          dataSource={assets}
          locale={{ emptyText: "暂无素材（可后续调用出图/详情接口）" }}
          renderItem={(a) => (
            <List.Item>
              #{a.id} · {a.type} · <Tag>{a.status}</Tag>
            </List.Item>
          )}
        />
      </Card>
    </PageContainer>
  );
}
