import { useRef } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import {
  ModalForm,
  PageContainer,
  ProFormTextArea,
  ProTable
} from "@ant-design/pro-components";
import { App, Button, Rate, Space, Tag } from "antd";
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
 * 商家评价管理（I15）
 * GET /seller/reviews · POST /seller/reviews/{id}/reply
 */
export function ReviewsPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();

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
      render: (_, r) => r.sellerReply || <Tag>未回复</Tag>
    },
    { title: "时间", dataIndex: "createdAt", valueType: "dateTime", width: 180 },
    {
      title: "操作",
      valueType: "option",
      width: 120,
      render: (_, r) => (
        <Space>
          <ModalForm
            title={`回复评价 #${r.id}`}
            trigger={<Button type="link">回复</Button>}
            initialValues={{ reply: r.sellerReply || "" }}
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
            <ProFormTextArea name="reply" label="回复内容" rules={[{ required: true }]} />
          </ModalForm>
        </Space>
      )
    }
  ];

  return (
    <PageContainer header={{ title: "评价管理", subTitle: "查看与回复买家评价" }}>
      <ProTable<Review>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        request={async () => {
          const data = await apiFetch<Review[]>("/api/v1/seller/reviews");
          return { data, success: true, total: data.length };
        }}
      />
    </PageContainer>
  );
}
