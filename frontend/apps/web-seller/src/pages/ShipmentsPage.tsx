import { useEffect, useRef, useState } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import {
  ModalForm,
  PageContainer,
  ProFormCheckbox,
  ProFormSelect,
  ProFormText,
  ProTable
} from "@ant-design/pro-components";
import { App, Button, Tag } from "antd";
import { apiFetch } from "@meiyue/api";

interface Order {
  id: number;
  orderNo: string;
  status: string;
  totalCents: number;
  items: { id: number; productTitle: string }[];
}

interface Shipment {
  id: number;
  orderId: number;
  status: string;
  carrierCode: string;
  trackingNo: string;
  packageSeq: number;
  ewaybillNo: string | null;
}

/**
 * 发货履约
 * API：GET /seller/orders · GET/POST /seller/shipments
 */
export function ShipmentsPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();
  const [orders, setOrders] = useState<Order[]>([]);

  useEffect(() => {
    apiFetch<Order[]>("/api/v1/seller/orders").then(setOrders).catch(() => undefined);
  }, []);

  const columns: ProColumns<Shipment>[] = [
    { title: "包裹 ID", dataIndex: "id", width: 80 },
    { title: "订单", dataIndex: "orderId" },
    { title: "序号", dataIndex: "packageSeq", width: 64 },
    { title: "承运商", dataIndex: "carrierCode" },
    { title: "运单号", dataIndex: "trackingNo" },
    { title: "面单号", dataIndex: "ewaybillNo" },
    {
      title: "状态",
      dataIndex: "status",
      render: (_, r) => <Tag>{r.status}</Tag>
    }
  ];

  return (
    <PageContainer header={{ title: "发货履约", subTitle: "多包裹 · MOCK 面单" }}>
      <ProTable<Shipment>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        toolBarRender={() => [
          <ModalForm
            key="ship"
            title="创建发货包裹"
            trigger={<Button type="primary">发货</Button>}
            onFinish={async (values) => {
              try {
                await apiFetch("/api/v1/seller/shipments", {
                  method: "POST",
                  json: {
                    orderId: Number(values.orderId),
                    carrierCode: values.carrierCode,
                    trackingNo: values.trackingNo || undefined,
                    printEwaybill: !!values.printEwaybill,
                    receiverName: "买家",
                    receiverPhone: "",
                    receiverAddress: ""
                  }
                });
                message.success("已创建包裹");
                actionRef.current?.reload();
                return true;
              } catch (err) {
                message.error(err instanceof Error ? err.message : "发货失败");
                return false;
              }
            }}
          >
            <ProFormSelect
              name="orderId"
              label="订单"
              options={orders.map((o) => ({
                label: `${o.orderNo} [${o.status}]`,
                value: o.id
              }))}
              rules={[{ required: true }]}
            />
            <ProFormSelect
              name="carrierCode"
              label="承运商"
              initialValue="SF"
              options={[
                { label: "顺丰 SF", value: "SF" },
                { label: "中通 ZTO", value: "ZTO" },
                { label: "圆通 YTO", value: "YTO" }
              ]}
            />
            <ProFormText name="trackingNo" label="运单号（可空，走 MOCK 面单）" />
            <ProFormCheckbox name="printEwaybill" initialValue={true}>
              打印 MOCK 电子面单
            </ProFormCheckbox>
          </ModalForm>
        ]}
        request={async () => {
          const data = await apiFetch<Shipment[]>("/api/v1/seller/shipments");
          return { data, success: true, total: data.length };
        }}
      />
    </PageContainer>
  );
}
