import { useEffect, useRef, useState } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { PageContainer, ProDescriptions, ProTable } from "@ant-design/pro-components";
import { App, Alert, Card, Popconfirm, Select, Space, Spin, Tag } from "antd";
import { apiFetch } from "@meiyue/api";
import type { AdminUserSummary, UserProfile } from "@meiyue/types";

/**
 * 账号与权限（I18）
 * - 当前登录平台管理员：GET /auth/me
 * - 用户目录：GET /admin/users?role= · POST /admin/users/{id}/status
 * 字段：id / username / displayName / phone / status / roles / createdAt
 */

export function AccountPage() {
  const { message } = App.useApp();
  const actionRef = useRef<ActionType>();
  const [me, setMe] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [roleFilter, setRoleFilter] = useState<string | undefined>(undefined);

  useEffect(() => {
    apiFetch<UserProfile>("/api/v1/auth/me")
      .then(setMe)
      .catch((e) => message.error(e instanceof Error ? e.message : "加载失败"))
      .finally(() => setLoading(false));
  }, [message]);

  const columns: ProColumns<AdminUserSummary>[] = [
    { title: "ID", dataIndex: "id", width: 72 },
    { title: "用户名", dataIndex: "username", copyable: true },
    { title: "显示名", dataIndex: "displayName" },
    { title: "手机", dataIndex: "phone", render: (_, r) => r.phone || "-" },
    {
      title: "角色",
      dataIndex: "roles",
      render: (_, r) => (
        <Space size={[4, 4]} wrap>
          {(r.roles || []).map((role) => (
            <Tag key={role}>{role}</Tag>
          ))}
        </Space>
      )
    },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      render: (_, r) => (
        <Tag color={r.status === "DISABLED" ? "red" : "green"}>{r.status}</Tag>
      )
    },
    {
      title: "操作",
      valueType: "option",
      width: 120,
      render: (_, r) => {
        const next = r.status === "DISABLED" ? "ENABLED" : "DISABLED";
        const label = next === "DISABLED" ? "禁用" : "启用";
        return (
          <Popconfirm
            title={`确认${label}账号 ${r.username}？`}
            disabled={me?.id === r.id && next === "DISABLED"}
            onConfirm={async () => {
              try {
                await apiFetch(`/api/v1/admin/users/${r.id}/status`, {
                  method: "POST",
                  json: { status: next }
                });
                message.success(`已${label}`);
                actionRef.current?.reload();
              } catch (err) {
                message.error(err instanceof Error ? err.message : "操作失败");
              }
            }}
          >
            <a style={{ opacity: me?.id === r.id && next === "DISABLED" ? 0.4 : 1 }}>{label}</a>
          </Popconfirm>
        );
      }
    }
  ];

  if (loading) {
    return (
      <PageContainer>
        <Spin />
      </PageContainer>
    );
  }

  return (
    <PageContainer header={{ title: "账号与权限", subTitle: "当前会话 · 用户目录" }}>
      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="用户目录"
        description="列表对接 GET /admin/users；支持按买家/商家角色过滤与启停。禁用后无法登录。不可禁用当前登录账号。"
      />
      <Card style={{ marginBottom: 16 }}>
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

      <ProTable<AdminUserSummary>
        headerTitle="用户目录"
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        toolBarRender={() => [
          <Select
            key="role"
            allowClear
            placeholder="按角色过滤"
            style={{ width: 180 }}
            value={roleFilter}
            onChange={(v) => {
              setRoleFilter(v);
              setTimeout(() => actionRef.current?.reload(), 0);
            }}
            options={[
              { label: "买家 BUYER", value: "BUYER" },
              { label: "店主 SELLER_OWNER", value: "SELLER_OWNER" },
              { label: "店员 SELLER_STAFF", value: "SELLER_STAFF" },
              { label: "平台 PLATFORM_ADMIN", value: "PLATFORM_ADMIN" }
            ]}
          />
        ]}
        params={{ role: roleFilter }}
        request={async (params) => {
          const qs = params.role ? `?role=${encodeURIComponent(String(params.role))}` : "";
          const data = await apiFetch<AdminUserSummary[]>(`/api/v1/admin/users${qs}`);
          return { data, success: true, total: data.length };
        }}
        pagination={{ pageSize: 20 }}
      />
    </PageContainer>
  );
}
