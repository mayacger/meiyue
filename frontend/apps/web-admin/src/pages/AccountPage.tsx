import { useEffect, useState } from "react";
import { PageContainer, ProDescriptions } from "@ant-design/pro-components";
import { App, Alert, Card, Spin, Tag } from "antd";
import { apiFetch } from "@meiyue/api";
import type { UserProfile } from "@meiyue/types";

/**
 * 账号与权限展示
 * - 当前登录平台管理员资料（GET /auth/me）
 * - 用户全量列表 API 尚未提供，本页仅展示当前会话与角色
 */
export function AccountPage() {
  const { message } = App.useApp();
  const [me, setMe] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiFetch<UserProfile>("/api/v1/auth/me")
      .then(setMe)
      .catch((e) => message.error(e instanceof Error ? e.message : "加载失败"))
      .finally(() => setLoading(false));
  }, [message]);

  if (loading) {
    return (
      <PageContainer>
        <Spin />
      </PageContainer>
    );
  }

  return (
    <PageContainer header={{ title: "账号与权限", subTitle: "当前会话 · 平台管理员" }}>
      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="用户目录只读说明"
        description="后端尚未提供 /admin/users 列表接口；本页展示当前登录账号与角色。种子账号：admin / admin123。"
      />
      <Card>
        {me ? (
          <ProDescriptions column={2} title="当前用户">
            <ProDescriptions.Item label="用户 ID">{me.id}</ProDescriptions.Item>
            <ProDescriptions.Item label="用户名">{me.username}</ProDescriptions.Item>
            <ProDescriptions.Item label="显示名">{me.displayName}</ProDescriptions.Item>
            <ProDescriptions.Item label="手机">{me.phone || "-"}</ProDescriptions.Item>
            <ProDescriptions.Item label="Actor">{me.actorType}</ProDescriptions.Item>
            <ProDescriptions.Item label="租户">{me.tenantId ?? "平台级"}</ProDescriptions.Item>
            <ProDescriptions.Item label="角色" span={2}>
              {me.roles.map((r) => (
                <Tag key={r} color="green">
                  {r}
                </Tag>
              ))}
            </ProDescriptions.Item>
          </ProDescriptions>
        ) : (
          <p>未获取到用户信息</p>
        )}
      </Card>
    </PageContainer>
  );
}
