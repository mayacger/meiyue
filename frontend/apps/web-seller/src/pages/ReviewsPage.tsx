import { useRef, useState } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import {
  ModalForm,
  PageContainer,
  ProFormTextArea,
  ProTable
} from "@ant-design/pro-components";
import { App, Button, Rate, Space, Tag, Segmented } from "antd";
import { apiFetch } from "@meiyue/api";

interface Review {
  id: number;
  productId: number;
  rating: number;
  content: string;
  sellerReply: string | null;
  createdAt: string;
}

/**
 * 商家评价管理（I21 体验）
 * GET /seller/reviews · POST /seller/reviews/{id}/reply
 * 支持「待回复」筛选与更醒目的回复入口
 */
export function ReviewsPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();
  const [filter, setFilter] = useState<"ALL" | "PENDING">("ALL");

  const columns: ProColumns<Review>[] = [
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
      title: "回复",
      dataIndex: "sellerReply",
      render: (_, r) =>
        r.sellerReply ? (
          <span>{r.sellerReply}</span>
        ) : (
          <Tag color="orange">待回复</Tag>
        )
    },
    { title: "时间", dataIndex: "createdAt", valueType: "dateTime", width: 180 },
    {
      title: "操作",
      valueType: "option",
      width: 140,
      render: (_, r) => (
        <Space>
          <ModalForm
            title={r.sellerReply ? `修改回复 #${r.id}` : `回复评价 #${r.id}`}
            trigger={
              <Button type={r.sellerReply ? "link" : "primary"} size="small">
                {r.sellerReply ? "修改回复" : "立即回复"}
              </Button>
            }
            initialValues={{ reply: r.sellerReply || "" }}
            modalProps={{ destroyOnClose: true }}
            onFinish={async (values) => {
              try {
                await apiFetch(`/api/v1/seller/reviews/${r.id}/reply`, {
                  method: "POST",
                  json: { reply: values.reply }
                });
                message.success("已回复");
                actionRef.current?.reload();
                return true;
              } catch (err) {
                message.error(err instanceof Error ? err.message : "回复失败");
                return false;
              }
            }}
          >
            <ProFormTextArea
              name="reply"
              label="回复内容"
              placeholder="感谢买家的反馈…"
              rules={[{ required: true, message: "请填写回复" }]}
              fieldProps={{ rows: 4, maxLength: 500, showCount: true }}
            />
          </ModalForm>
        </Space>
      )
    }
  ];

  return (
    <PageContainer
      header={{
        title: "评价管理",
        subTitle: "优先处理待回复评价"
      }}
    >
      <ProTable<Review>
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
              setFilter(v as "ALL" | "PENDING");
              setTimeout(() => actionRef.current?.reload(), 0);
            }}
            options={[
              { label: "全部", value: "ALL" },
              { label: "待回复", value: "PENDING" }
            ]}
          />
        ]}
        request={async (params) => {
          let data = await apiFetch<Review[]>("/api/v1/seller/reviews");
          if (params.filter === "PENDING") {
            data = data.filter((r) => !r.sellerReply);
          }
          return { data, success: true, total: data.length };
        }}
        pagination={{ pageSize: 20 }}
      />
    </PageContainer>
  );
}
