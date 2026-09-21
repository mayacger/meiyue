import { View, Text, Button } from "@tarojs/components";
import Taro, { useDidShow } from "@tarojs/taro";
import { useState } from "react";
import type { OrderSummary } from "@meiyue/types";
import { apiFetch, getToken } from "../../services/api";
import "./index.css";

/**
 * 订单列表（I16）
 * GET /buyer/orders · 模拟支付 · 进详情
 */
export default function OrdersPage() {
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [tip, setTip] = useState("");

  useDidShow(() => {
    if (!getToken()) {
      setTip("请先登录");
      setOrders([]);
      return;
    }
    apiFetch<OrderSummary[]>("/api/v1/buyer/orders")
      .then((list) => {
        setOrders(list);
        setTip("");
      })
      .catch((e) => setTip(e instanceof Error ? e.message : "加载失败"));
  });

  async function mockPay(id: number) {
    try {
      await apiFetch(`/api/v1/buyer/orders/${id}/mock-pay`, { method: "POST" });
      Taro.showToast({ title: "支付成功", icon: "success" });
      const list = await apiFetch<OrderSummary[]>("/api/v1/buyer/orders");
      setOrders(list);
    } catch (e) {
      Taro.showToast({ title: e instanceof Error ? e.message : "支付失败", icon: "none" });
    }
  }

  return (
    <View className="page">
      <Text className="h1">我的订单</Text>
      {tip ? <Text className="muted">{tip}</Text> : null}
      {orders.map((o) => (
        <View key={o.id} className="row">
          <View
            className="meta"
            onClick={() => Taro.navigateTo({ url: `/pages/order-detail/index?id=${o.id}` })}
          >
            <Text className="no">{o.orderNo}</Text>
            <Text className="muted">
              [{o.status}] ¥{(o.totalCents / 100).toFixed(2)}
            </Text>
          </View>
          {o.status === "PENDING_PAYMENT" ? (
            <Button size="mini" className="btn" onClick={() => mockPay(o.id)}>
              模拟支付
            </Button>
          ) : (
            <Button
              size="mini"
              className="btn ghost"
              onClick={() => Taro.navigateTo({ url: `/pages/order-detail/index?id=${o.id}` })}
            >
              详情
            </Button>
          )}
        </View>
      ))}
      {!tip && orders.length === 0 ? <Text className="muted">暂无订单</Text> : null}
    </View>
  );
}
