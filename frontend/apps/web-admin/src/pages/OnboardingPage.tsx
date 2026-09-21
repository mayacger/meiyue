import { useRef } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { PageContainer, ProTable } from "@ant-design/pro-components";
import { App, Button, Space, Tag } from "antd";
import { apiFetch } from "@meiyue/api";
import type { OnboardingApplication } from "@meiyue/types";

/**
 * 入驻审核页（ProTable）
 * API：GET /admin/onboarding/pending；POST approve / reject
 * 字段：shopName / shopSlug / contactName / contactPhone / applicantUserId
 */
export function OnboardingPage() {
  const actionRef = useRef<ActionType>();
  const { message, modal } = App.useApp();

  const columns: ProColumns<OnboardingApplication>[] = [
    { title: "ID", dataIndex: "id", width: 72 },
    { title: "店铺名", dataIndex: "shopName" },
    { title: "Slug", dataIndex: "shopSlug", copyable: true },
    { title: "联系人", dataIndex: "contactName" },
    { title: "电话", dataIndex: "contactPhone" },
    { title: "申请人 UID", dataIndex: "applicantUserId", width: 110 },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      render: (_, row) => <Tag color="processing">{row.status}</Tag>
    },
    {
      title: "操作",
      valueType: "option",
      width: 180,
      render: (_, row) => (
        <Space>
          <Button
            type="link"
            onClick={async () => {
              try {
                await apiFetch(`/api/v1/admin/onboarding/${row.id}/approve`, {
                  method: "POST",
                  json: { reviewNote: "平台审核通过" }
                });
                message.success("已通过并开店");
                actionRef.current?.reload();
              } catch (err) {
                message.error(err instanceof Error ? err.message : "操作失败");
              }
            }}
          >
            通过
          </Button>
          <Button
            type="link"
            danger
            onClick={() => {
              modal.confirm({
                title: "拒绝入驻申请",
                content: `确认拒绝「${row.shopName}」？`,
                onOk: async () => {
                  try {
                    await apiFetch(`/api/v1/admin/onboarding/${row.id}/reject`, {
                      method: "POST",
                      json: { reviewNote: "资料不完整" }
                    });
                    message.success("已拒绝");
                    actionRef.current?.reload();
                  } catch (err) {
                    message.error(err instanceof Error ? err.message : "操作失败");
                  }
                }
              });
            }}
          >
            拒绝
          </Button>
        </Space>
      )
    }
  ];

  return (
    <PageContainer header={{ title: "入驻审核", subTitle: "待审申请列表" }}>
      <ProTable<OnboardingApplication>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        request={async () => {
          const data = await apiFetch<OnboardingApplication[]>(
            "/api/v1/admin/onboarding/pending"
          );
          return { data, success: true, total: data.length };
        }}
        pagination={{ pageSize: 10 }}
      />
    </PageContainer>
  );
}
