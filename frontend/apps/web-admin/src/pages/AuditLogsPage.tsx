import { useRef } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { PageContainer, ProTable } from "@ant-design/pro-components";
import { Tag } from "antd";
import { apiFetch } from "@meiyue/api";

/**
 * 审计日志只读（I24）
 * API：GET /admin/audit-logs?action=&actorType=&resourceType=&tenantId=&page=&size=
 * 字段：actorUserId / actorType / action / resourceType / outcome / detail / createdAt
 */
interface AuditRow {
  id: number;
  actorUserId: number | null;
  actorType: string;
  tenantId: number | null;
  action: string;
  resourceType: string | null;
  resourceId: string | null;
  outcome: string;
  detail: string | null;
  traceId: string | null;
  ip: string | null;
  createdAt: string | null;
}

interface AuditPage {
  content: AuditRow[];
  totalElements: number;
}

export function AuditLogsPage() {
  const actionRef = useRef<ActionType>();

  const columns: ProColumns<AuditRow>[] = [
    { title: "ID", dataIndex: "id", width: 72, search: false },
    {
      title: "动作",
      dataIndex: "action",
      width: 160,
      fieldProps: { placeholder: "如 PRODUCT_UPDATE" }
    },
    {
      title: "主体类型",
      dataIndex: "actorType",
      width: 110,
      valueEnum: {
        BUYER: { text: "BUYER" },
        SELLER: { text: "SELLER" },
        PLATFORM: { text: "PLATFORM" },
        SYSTEM: { text: "SYSTEM" }
      }
    },
    {
      title: "资源类型",
      dataIndex: "resourceType",
      width: 120,
      fieldProps: { placeholder: "如 Product" }
    },
    {
      title: "租户",
      dataIndex: "tenantId",
      width: 90,
      valueType: "digit",
      fieldProps: { precision: 0 }
    },
    { title: "用户", dataIndex: "actorUserId", width: 90, search: false },
    { title: "资源 ID", dataIndex: "resourceId", width: 100, search: false, ellipsis: true },
    {
      title: "结果",
      dataIndex: "outcome",
      width: 90,
      search: false,
      render: (_, r) => (
        <Tag color={r.outcome === "SUCCESS" ? "green" : "red"}>{r.outcome}</Tag>
      )
    },
    { title: "摘要", dataIndex: "detail", ellipsis: true, search: false },
    { title: "时间", dataIndex: "createdAt", valueType: "dateTime", width: 180, search: false }
  ];

  return (
    <PageContainer header={{ title: "审计日志", subTitle: "只读筛选 · 不含密钥明文" }}>
      <ProTable<AuditRow>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        search={{ labelWidth: "auto" }}
        request={async (params) => {
          const q = new URLSearchParams();
          q.set("page", String((params.current ?? 1) - 1));
          q.set("size", String(params.pageSize ?? 20));
          if (params.action) q.set("action", String(params.action));
          if (params.actorType) q.set("actorType", String(params.actorType));
          if (params.resourceType) q.set("resourceType", String(params.resourceType));
          if (params.tenantId != null && params.tenantId !== "") {
            q.set("tenantId", String(params.tenantId));
          }
          const data = await apiFetch<AuditPage>(`/api/v1/admin/audit-logs?${q.toString()}`);
          return {
            data: data.content,
            success: true,
            total: data.totalElements
          };
        }}
      />
    </PageContainer>
  );
}
