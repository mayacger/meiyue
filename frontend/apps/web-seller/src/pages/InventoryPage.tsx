import { useRef } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import {
  ModalForm,
  PageContainer,
  ProFormDigit,
  ProTable
} from "@ant-design/pro-components";
import { App, Button, Tag } from "antd";
import { apiFetch } from "@meiyue/api";

/**
 * 商家库存管理（I20）
 * API：GET /seller/inventory · POST /seller/inventory/skus/{skuId}/stock
 * 字段：skuId / productTitle / skuCode / specText / priceCents / stockQty / productStatus
 */
interface InventorySku {
  skuId: number;
  productId: number;
  productTitle: string;
  skuCode: string;
  specText?: string | null;
  priceCents: number;
  stockQty: number;
  productStatus: string;
}

export function InventoryPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();

  const columns: ProColumns<InventorySku>[] = [
    { title: "SKU ID", dataIndex: "skuId", width: 90 },
    { title: "商品", dataIndex: "productTitle", ellipsis: true },
    { title: "SKU", dataIndex: "skuCode", copyable: true },
    { title: "规格", dataIndex: "specText", render: (_, r) => r.specText || "-" },
    {
      title: "单价",
      dataIndex: "priceCents",
      width: 100,
      render: (_, r) => `¥${(r.priceCents / 100).toFixed(2)}`
    },
    {
      title: "库存",
      dataIndex: "stockQty",
      width: 90,
      render: (_, r) => (
        <Tag color={r.stockQty <= 5 ? "orange" : "green"}>{r.stockQty}</Tag>
      )
    },
    {
      title: "商品状态",
      dataIndex: "productStatus",
      width: 110,
      render: (_, r) => <Tag>{r.productStatus}</Tag>
    },
    {
      title: "操作",
      valueType: "option",
      width: 120,
      render: (_, r) => (
        <ModalForm
          title={`调整库存 · ${r.skuCode}`}
          trigger={<Button type="link">调整</Button>}
          initialValues={{ stockQty: r.stockQty }}
          onFinish={async (values) => {
            try {
              await apiFetch(`/api/v1/seller/inventory/skus/${r.skuId}/stock`, {
                method: "POST",
                json: { stockQty: Number(values.stockQty) }
              });
              message.success("库存已更新");
              actionRef.current?.reload();
              return true;
            } catch (err) {
              message.error(err instanceof Error ? err.message : "调整失败");
              return false;
            }
          }}
        >
          <ProFormDigit
            name="stockQty"
            label="目标库存"
            min={0}
            rules={[{ required: true, message: "请输入库存" }]}
          />
        </ModalForm>
      )
    }
  ];

  return (
    <PageContainer header={{ title: "库存管理", subTitle: "按 SKU 调整可售库存" }}>
      <ProTable<InventorySku>
        actionRef={actionRef}
        rowKey="skuId"
        search={false}
        columns={columns}
        request={async () => {
          const data = await apiFetch<InventorySku[]>("/api/v1/seller/inventory");
          return { data, success: true, total: data.length };
        }}
        pagination={{ pageSize: 20 }}
      />
    </PageContainer>
  );
}
