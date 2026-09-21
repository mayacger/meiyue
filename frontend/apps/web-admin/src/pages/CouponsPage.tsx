import { useRef } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import {
  ModalForm,
  PageContainer,
  ProFormDigit,
  ProFormText,
  ProTable
} from "@ant-design/pro-components";
import { App, Button, Tag } from "antd";
import { apiFetch } from "@meiyue/api";
import type { CouponSummary } from "@meiyue/types";

/**
 * 平台券管理
 * API：GET /platform-coupons；POST /admin/platform-coupons
 * 字段：code / title / discountCents / minSpendCents / totalQuota
 * 规则：与店券默认互斥（MUTUAL_EXCLUSIVE）
 */
export function CouponsPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();

  const columns: ProColumns<CouponSummary>[] = [
    { title: "券码", dataIndex: "code", copyable: true },
    { title: "标题", dataIndex: "title" },
    {
      title: "面额",
      dataIndex: "discountCents",
      render: (_, r) => `¥${(r.discountCents / 100).toFixed(2)}`
    },
    {
      title: "门槛",
      dataIndex: "minSpendCents",
      render: (_, r) => `¥${(r.minSpendCents / 100).toFixed(2)}`
    },
    {
      title: "领取",
      render: (_, r) => `${r.claimedCount ?? 0}/${r.totalQuota || "∞"}`
    },
    {
      title: "状态",
      dataIndex: "status",
      render: (_, r) => <Tag>{r.status ?? "ACTIVE"}</Tag>
    }
  ];

  return (
    <PageContainer
      header={{
        title: "平台券",
        subTitle: "跨店可用；与店券互斥"
      }}
    >
      <ProTable<CouponSummary>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        toolBarRender={() => [
          <ModalForm
            key="create"
            title="创建平台券"
            trigger={<Button type="primary">发放平台券</Button>}
            onFinish={async (values) => {
              try {
                await apiFetch("/api/v1/admin/platform-coupons", {
                  method: "POST",
                  json: {
                    code: values.code,
                    title: values.title,
                    discountCents: Math.round(Number(values.discountYuan) * 100),
                    minSpendCents: Math.round(Number(values.minYuan) * 100),
                    totalQuota: Number(values.quota)
                  }
                });
                message.success("已创建");
                actionRef.current?.reload();
                return true;
              } catch (err) {
                message.error(err instanceof Error ? err.message : "创建失败");
                return false;
              }
            }}
          >
            <ProFormText name="code" label="券码" initialValue="PLAT10" rules={[{ required: true }]} />
            <ProFormText name="title" label="标题" initialValue="平台满减10元" rules={[{ required: true }]} />
            <ProFormDigit name="discountYuan" label="面额（元）" initialValue={10} min={0.01} rules={[{ required: true }]} />
            <ProFormDigit name="minYuan" label="门槛（元）" initialValue={50} min={0} rules={[{ required: true }]} />
            <ProFormDigit name="quota" label="总量" initialValue={1000} min={1} rules={[{ required: true }]} />
          </ModalForm>
        ]}
        request={async () => {
          const data = await apiFetch<CouponSummary[]>("/api/v1/platform-coupons");
          return { data, success: true, total: data.length };
        }}
      />
    </PageContainer>
  );
}
