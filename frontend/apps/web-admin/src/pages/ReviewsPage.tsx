import { useRef, useState } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import {
  ModalForm,
  PageContainer,
  ProFormTextArea,
  ProTable
} from "@ant-design/pro-components";
import { App, Button, Rate, Segmented, Space, Tag } from "antd";
import { apiFetch } from "@meiyue/api";
import type { ProductReview } from "@meiyue/types";

/**
 * 平台评价审核（I30）
 *
 * 入口：AdminLayout → /reviews
 * API：
 *   GET  /api/v1/admin/reviews
 *   POST /api/v1/admin/reviews/{id}/hide   body: { reason }
 *   POST /api/v1/admin/reviews/{id}/restore
 *
 * 关系：隐藏后 Buyer GET /products/{id}/reviews 不再返回该条
 */
export function ReviewsPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();
  const [filter, setFilter] = useState<"ALL" | "HIDDEN" | "VISIBLE">("ALL");

  const columns: ProColumns<ProductReview>[] = [
    { title: "ID", dataIndex: "id", width: 72 },
    { title: "商品", dataIndex: "productId", width: 90 },
    {
      title: "评分",
      dataIndex: "rating",
      width: 140,
      render: (_, r) => <Rate disabled value={r.rating} />
    },
    { title: "内容", dataIndex: "content", ellipsis: true },
    {
      title: "状态",
      dataIndex: "hidden",
      width: 100,
      render: (_, r) =>
        r.hidden ? <Tag color="error">已隐藏</Tag> : <Tag color="success">展示中</Tag>
    },
    {
      title: "隐藏原因",
      dataIndex: "hiddenReason",
      ellipsis: true,
      render: (_, r) => r.hiddenReason || "—"
    },
    { title: "时间", dataIndex: "createdAt", valueType: "dateTime", width: 180 },
    {
      title: "操作",
      valueType: "option",
      width: 180,
      render: (_, r) => (
        <Space>
          {r.hidden ? (
            <Button
              type="link"
              size="small"
              onClick={async () => {
                try {
                  await apiFetch(`/api/v1/admin/reviews/${r.id}/restore`, { method: "POST" });
                  message.success("已恢复展示");
                  actionRef.current?.reload();
                } catch (err) {
                  message.error(err instanceof Error ? err.message : "恢复失败");
                }
              }}
            >
              恢复
            </Button>
          ) : (
            <ModalForm
              title={`隐藏评价 #${r.id}`}
              trigger={
                <Button type="link" danger size="small">
                  隐藏
                </Button>
              }
              initialValues={{ reason: "不当内容" }}
              modalProps={{ destroyOnClose: true }}
              onFinish={async (values) => {
                try {
                  await apiFetch(`/api/v1/admin/reviews/${r.id}/hide`, {
                    method: "POST",
                    json: { reason: values.reason }
                  });
                  message.success("已隐藏");
                  actionRef.current?.reload();
                  return true;
                } catch (err) {
                  message.error(err instanceof Error ? err.message : "隐藏失败");
                  return false;
                }
              }}
            >
              <ProFormTextArea
                name="reason"
                label="隐藏原因"
                rules={[{ required: true, message: "请填写原因" }]}
                fieldProps={{ rows: 3, maxLength: 200, showCount: true }}
              />
            </ModalForm>
          )}
        </Space>
      )
    }
  ];

  return (
    <PageContainer
      header={{
        title: "评价审核",
        subTitle: "隐藏不当评价 · 买家公开列表自动过滤"
      }}
    >
      <ProTable<ProductReview>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        params={{ filter }}
        toolBarRender={() => [
          <Segmented
            key="filter"
            value={filter}
            onChange={(v) => {
              setFilter(v as "ALL" | "HIDDEN" | "VISIBLE");
              setTimeout(() => actionRef.current?.reload(), 0);
            }}
            options={[
              { label: "全部", value: "ALL" },
              { label: "展示中", value: "VISIBLE" },
              { label: "已隐藏", value: "HIDDEN" }
            ]}
          />
        ]}
        request={async () => {
          const data = await apiFetch<ProductReview[]>("/api/v1/admin/reviews");
          const filtered =
            filter === "HIDDEN"
              ? data.filter((r) => r.hidden)
              : filter === "VISIBLE"
                ? data.filter((r) => !r.hidden)
                : data;
          return { data: filtered, success: true, total: filtered.length };
        }}
      />
    </PageContainer>
  );
}
