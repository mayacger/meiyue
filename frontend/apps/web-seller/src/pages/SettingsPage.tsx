import { useEffect, useState } from "react";
import {
  PageContainer,
  ProCard,
  ProForm,
  ProFormText
} from "@ant-design/pro-components";
import { App, Descriptions } from "antd";
import { apiFetch } from "@meiyue/api";
import type { UserProfile } from "@meiyue/types";

/**
 * 商家账号设置（I24）
 * API：GET /auth/me · PUT /auth/profile · POST /auth/password
 * 入口：SellerLayout /settings
 */
export function SettingsPage() {
  const { message } = App.useApp();
  const [me, setMe] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  async function reload() {
    setLoading(true);
    try {
      setMe(await apiFetch<UserProfile>("/api/v1/auth/me"));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload().catch((e) => message.error(e instanceof Error ? e.message : "加载失败"));
  }, [message]);

  return (
    <PageContainer header={{ title: "账号设置", subTitle: "资料与改密" }} loading={loading}>
      {me ? (
        <ProCard gutter={16} wrap>
          <ProCard colSpan={12} title="当前账号" bordered>
            <Descriptions column={1} size="small">
              <Descriptions.Item label="用户名">{me.username}</Descriptions.Item>
              <Descriptions.Item label="展示名">{me.displayName}</Descriptions.Item>
              <Descriptions.Item label="手机">{me.phone || "—"}</Descriptions.Item>
              <Descriptions.Item label="角色">{me.roles?.join(", ")}</Descriptions.Item>
              <Descriptions.Item label="租户">{me.tenantId ?? "—"}</Descriptions.Item>
            </Descriptions>
          </ProCard>
          <ProCard colSpan={12} title="更新资料" bordered>
            <ProForm
              initialValues={{ displayName: me.displayName, phone: me.phone || "" }}
              onFinish={async (values) => {
                try {
                  const u = await apiFetch<UserProfile>("/api/v1/auth/profile", {
                    method: "PUT",
                    json: { displayName: values.displayName, phone: values.phone || "" }
                  });
                  setMe(u);
                  message.success("资料已保存");
                  return true;
                } catch (err) {
                  message.error(err instanceof Error ? err.message : "保存失败");
                  return false;
                }
              }}
            >
              <ProFormText name="displayName" label="展示名" rules={[{ required: true, max: 128 }]} />
              <ProFormText name="phone" label="手机" rules={[{ max: 32 }]} />
            </ProForm>
          </ProCard>
          <ProCard colSpan={12} title="修改密码" bordered style={{ marginTop: 16 }}>
            <ProForm
              onFinish={async (values) => {
                try {
                  await apiFetch("/api/v1/auth/password", {
                    method: "POST",
                    json: {
                      oldPassword: values.oldPassword,
                      newPassword: values.newPassword
                    }
                  });
                  message.success("密码已更新");
                  return true;
                } catch (err) {
                  message.error(err instanceof Error ? err.message : "改密失败");
                  return false;
                }
              }}
            >
              <ProFormText.Password
                name="oldPassword"
                label="当前密码"
                rules={[{ required: true }]}
              />
              <ProFormText.Password
                name="newPassword"
                label="新密码"
                rules={[{ required: true, min: 6, max: 64 }]}
              />
            </ProForm>
          </ProCard>
        </ProCard>
      ) : null}
    </PageContainer>
  );
}
