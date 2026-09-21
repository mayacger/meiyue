import { View, Text } from "@tarojs/components";
import { useDidShow } from "@tarojs/taro";
import { useState } from "react";
import type { CartItem } from "@meiyue/types";
import { apiFetch, getToken } from "../../services/api";
import "./index.css";

/** 购物车骨架：登录后拉取 /buyer/cart */
export default function CartPage() {
  const [cart, setCart] = useState<CartItem[]>([]);
  const [tip, setTip] = useState("");

  useDidShow(() => {
    if (!getToken()) {
      setTip("请先在「我的」登录");
      setCart([]);
      return;
    }
    setTip("");
    apiFetch<CartItem[]>("/api/v1/buyer/cart")
      .then(setCart)
      .catch((e) => setTip(e instanceof Error ? e.message : "加载失败"));
  });

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
    </View>
  );
}
