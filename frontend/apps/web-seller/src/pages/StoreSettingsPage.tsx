import { useEffect, useState } from "react";
import {
  PageContainer,
  ProForm,
  ProFormDigit,
  ProFormText,
  ProFormTextArea,
  ProCard
} from "@ant-design/pro-components";
import { App, Descriptions, Image } from "antd";
import { apiFetch } from "@meiyue/api";
import type { StoreInfo } from "@meiyue/types";

/**
 * 店铺设置（I22 + I31 运费模板）
 *
 * API：GET/PUT /api/v1/seller/store
 * 字段：name / description / logoUrl / freightCents / freeShippingThresholdCents
 *
 * 关系：SellerLayout → /store → 买家结算按店计运费
 * 包邮门槛：留空或传 -1 表示无包邮
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
      header={{
        title: "店铺设置",
        subTitle: "店名 / 简介 / Logo / 运费模板 · 买家结算同步"
      }}
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
              <Descriptions.Item label="默认运费">
                ¥{((store.freightCents ?? 0) / 100).toFixed(2)}
              </Descriptions.Item>
              <Descriptions.Item label="包邮门槛">
                {store.freeShippingThresholdCents != null
                  ? `¥${(store.freeShippingThresholdCents / 100).toFixed(2)}`
                  : "无包邮"}
              </Descriptions.Item>
            </Descriptions>
          </ProCard>
          <ProCard colSpan={12} title="编辑" bordered>
            <ProForm
              initialValues={{
                name: store.name,
                description: store.description || "",
                logoUrl: store.logoUrl || "",
                freightYuan: (store.freightCents ?? 0) / 100,
                freeShipYuan:
                  store.freeShippingThresholdCents != null
                    ? store.freeShippingThresholdCents / 100
                    : undefined
              }}
              onFinish={async (values) => {
                try {
                  const updated = await apiFetch<StoreInfo>("/api/v1/seller/store", {
                    method: "PUT",
                    json: {
                      name: values.name,
                      description: values.description || "",
                      logoUrl: values.logoUrl || "",
                      freightCents: Math.round(Number(values.freightYuan || 0) * 100),
                      // 未填包邮门槛 → -1 清空为无包邮
                      freeShippingThresholdCents:
                        values.freeShipYuan === undefined ||
                        values.freeShipYuan === null ||
                        values.freeShipYuan === ""
                          ? -1
                          : Math.round(Number(values.freeShipYuan) * 100)
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
              <ProFormDigit
                name="freightYuan"
                label="默认运费（元）"
                min={0}
                fieldProps={{ precision: 2, step: 1 }}
                extra="未达包邮门槛时收取；0 表示免运费"
              />
              <ProFormDigit
                name="freeShipYuan"
                label="包邮门槛（元）"
                min={0}
                fieldProps={{ precision: 2, step: 10 }}
                extra="留空表示无包邮；达到门槛则本店运费为 0"
              />
            </ProForm>
          </ProCard>
        </ProCard>
      ) : null}
    </PageContainer>
  );
}
