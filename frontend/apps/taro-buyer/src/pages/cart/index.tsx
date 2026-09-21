import { View, Text, Button } from "@tarojs/components";
import Taro, { useDidShow } from "@tarojs/taro";
import { useState } from "react";
import type { CartItem, OrderSummary } from "@meiyue/types";
import { apiFetch, getToken } from "../../services/api";
import "./index.css";

/**
 * 购物车 + 结算下单（I15）
 * API：GET /buyer/cart · POST /buyer/orders/checkout
 */
export default function CartPage() {
  const [cart, setCart] = useState<CartItem[]>([]);
  const [tip, setTip] = useState("");
  const [busy, setBusy] = useState(false);

  async function reload() {
    if (!getToken()) {
      setTip("请先在「我的」登录");
      setCart([]);
      return;
    }
    setTip("");
    setCart(await apiFetch<CartItem[]>("/api/v1/buyer/cart"));
  }

  useDidShow(() => {
    reload().catch((e) => setTip(e instanceof Error ? e.message : "加载失败"));
  });

  async function checkout() {
    if (!cart.length || busy) return;
    setBusy(true);
    try {
      const order = await apiFetch<OrderSummary>("/api/v1/buyer/orders/checkout", {
        method: "POST",
        data: {}
      });
      Taro.showToast({ title: `下单成功 ${order.orderNo}`, icon: "success" });
      await reload();
      Taro.switchTab({ url: "/pages/mine/index" });
    } catch (e) {
      Taro.showToast({
        title: e instanceof Error ? e.message : "下单失败",
        icon: "none"
      });
    } finally {
      setBusy(false);
    }
  }

  const total = cart.reduce((s, c) => s + c.lineTotalCents, 0);

  return (
    <View className="page">
      <Text className="h1">购物车</Text>
      {tip ? <Text className="muted">{tip}</Text> : null}
      {cart.map((c) => (
        <View key={c.id} className="row">
          <Text>
            {c.productTitle} ×{c.quantity}
          </Text>
          <Text className="price">¥{(c.lineTotalCents / 100).toFixed(2)}</Text>
        </View>
      ))}
      {!tip && cart.length === 0 ? <Text className="muted">购物车为空</Text> : null}
      {cart.length > 0 ? (
        <View className="bar">
          <Text>
            合计 <Text className="price">¥{(total / 100).toFixed(2)}</Text>
          </Text>
          <Button className="btn" loading={busy} onClick={checkout}>
            结算下单
          </Button>
        </View>
      ) : null}
    </View>
  );
}
