import { useEffect, useState } from "react";
import {
  PageContainer,
  ProForm,
  ProFormText,
  ProFormTextArea,
  ProCard
} from "@ant-design/pro-components";
import { App, Descriptions, Image } from "antd";
import { apiFetch } from "@meiyue/api";
import type { StoreInfo } from "@meiyue/types";

/**
 * 店铺设置（I22）
 * API：GET/PUT /api/v1/seller/store
 * 字段：name 店名 / description 简介 / logoUrl Logo URL（占位，非强制 OSS）
 *
 * 关系：SellerLayout → /store → 买家 GET /stores/{tenantId} 展示
 */
export function StoreSettingsPage() {
  const { message } = App.useApp();
  const [store, setStore] = useState<StoreInfo | null>(null);
  const [loading, setLoading] = useState(true);

  async function reload() {
    setLoading(true);
    try {
      setStore(await apiFetch<StoreInfo>("/api/v1/seller/store"));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload().catch((e) => message.error(e instanceof Error ? e.message : "加载失败"));
  }, [message]);

  return (
    <PageContainer
      header={{ title: "店铺设置", subTitle: "店名 / 简介 / Logo URL · 买家店页同步展示" }}
      loading={loading}
    >
      {store ? (
        <ProCard gutter={16} wrap>
          <ProCard colSpan={12} title="当前资料" bordered>
            <Descriptions column={1} size="small">
              <Descriptions.Item label="租户 ID">{store.tenantId}</Descriptions.Item>
              <Descriptions.Item label="Slug">{store.slug}</Descriptions.Item>
              <Descriptions.Item label="状态">{store.status}</Descriptions.Item>
              <Descriptions.Item label="店名">{store.name}</Descriptions.Item>
              <Descriptions.Item label="简介">{store.description || "—"}</Descriptions.Item>
              <Descriptions.Item label="Logo">
                {store.logoUrl ? (
                  <Image src={store.logoUrl} width={72} alt="logo" />
                ) : (
                  "未设置"
                )}
              </Descriptions.Item>
            </Descriptions>
          </ProCard>
          <ProCard colSpan={12} title="编辑" bordered>
            <ProForm
              initialValues={{
                name: store.name,
                description: store.description || "",
                logoUrl: store.logoUrl || ""
              }}
              onFinish={async (values) => {
                try {
                  const updated = await apiFetch<StoreInfo>("/api/v1/seller/store", {
                    method: "PUT",
                    json: {
                      name: values.name,
                      description: values.description || "",
                      logoUrl: values.logoUrl || ""
                    }
                  });
                  setStore(updated);
                  message.success("店铺资料已保存");
                  return true;
                } catch (err) {
                  message.error(err instanceof Error ? err.message : "保存失败");
                  return false;
                }
              }}
            >
              <ProFormText name="name" label="店名" rules={[{ required: true, max: 128 }]} />
              <ProFormTextArea
                name="description"
                label="简介"
                fieldProps={{ maxLength: 512, showCount: true, rows: 4 }}
              />
              <ProFormText
                name="logoUrl"
                label="Logo URL"
                placeholder="https://… 或占位图地址"
                rules={[{ max: 1024 }]}
              />
            </ProForm>
          </ProCard>
        </ProCard>
      ) : null}
    </PageContainer>
  );
}
