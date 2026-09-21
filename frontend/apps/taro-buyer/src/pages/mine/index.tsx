import { View, Text, Input, Button } from "@tarojs/components";
import { useState } from "react";
import Taro, { useDidShow } from "@tarojs/taro";
import type { AuthResult, OrderSummary, UserProfile } from "@meiyue/types";
import { apiFetch, getToken, setToken } from "../../services/api";
import "./index.css";

/**
 * 我的（I16 完善）
 * 登录 / 订单入口 / 地址占位 / 模拟支付快捷
 */
export default function MinePage() {
  const [username, setUsername] = useState("buyer1");
  const [password, setPassword] = useState("buyer123");
  const [me, setMe] = useState<UserProfile | null>(null);
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [error, setError] = useState("");

  async function loadMe() {
    if (!getToken()) {
      setMe(null);
      setOrders([]);
      return;
    }
    try {
      setMe(await apiFetch<UserProfile>("/api/v1/auth/me"));
      setOrders(await apiFetch<OrderSummary[]>("/api/v1/buyer/orders"));
    } catch {
      setMe(null);
      setOrders([]);
    }
  }

  useDidShow(() => {
    loadMe();
  });

  async function login() {
    setError("");
    try {
      const data = await apiFetch<AuthResult>("/api/v1/auth/login", {
        method: "POST",
        data: { username, password }
      });
      setToken(data.accessToken);
      setMe(data.user);
      setOrders(await apiFetch<OrderSummary[]>("/api/v1/buyer/orders"));
      Taro.showToast({ title: "登录成功", icon: "success" });
    } catch (e) {
      setError(e instanceof Error ? e.message : "登录失败");
    }
  }

  function logout() {
    setToken(null);
    setMe(null);
    setOrders([]);
  }

  async function mockPay(id: number) {
    try {
      await apiFetch(`/api/v1/buyer/orders/${id}/mock-pay`, { method: "POST" });
      Taro.showToast({ title: "支付成功", icon: "success" });
      setOrders(await apiFetch<OrderSummary[]>("/api/v1/buyer/orders"));
    } catch (e) {
      Taro.showToast({
        title: e instanceof Error ? e.message : "支付失败",
        icon: "none"
      });
    }
  }

  return (
    <View className="page">
      <Text className="brand">美月商城</Text>
      <Text className="h1">我的</Text>
      {me ? (
        <View className="card">
          <Text>已登录：{me.displayName || me.username}</Text>
          <Text className="muted">角色：{me.roles.join(", ")}</Text>
          <View className="links">
            <Button
              className="btn ghost"
              size="mini"
              onClick={() => Taro.navigateTo({ url: "/pages/orders/index" })}
            >
              全部订单
            </Button>
            <Button
              className="btn ghost"
              size="mini"
              onClick={() => Taro.navigateTo({ url: "/pages/address/index" })}
            >
              收货地址
            </Button>
            <Button
              className="btn ghost"
              size="mini"
              onClick={() => Taro.navigateTo({ url: "/pages/notifications/index" })}
            >
              站内通知
            </Button>
            <Button className="btn ghost" size="mini" onClick={logout}>
              退出
            </Button>
          </View>
        </View>
      ) : (
        <View className="card">
          <Input
            className="input"
            value={username}
            onInput={(e) => setUsername(e.detail.value)}
            placeholder="用户名"
          />
          <Input
            className="input"
            password
            value={password}
            onInput={(e) => setPassword(e.detail.value)}
            placeholder="密码"
          />
          {error ? <Text className="err">{error}</Text> : null}
          <Button className="btn" onClick={login}>
            登录
          </Button>
        </View>
      )}

      {me ? (
        <View className="orders">
          <Text className="h2">最近订单</Text>
          {orders.length === 0 ? <Text className="muted">暂无订单</Text> : null}
          {orders.slice(0, 5).map((o) => (
            <View key={o.id} className="order-row">
              <View onClick={() => Taro.navigateTo({ url: `/pages/order-detail/index?id=${o.id}` })}>
                <Text className="order-no">{o.orderNo}</Text>
                <Text className="muted">
                  [{o.status}] ¥{(o.totalCents / 100).toFixed(2)}
                </Text>
              </View>
              {o.status === "PENDING_PAYMENT" ? (
                <Button size="mini" className="btn" onClick={() => mockPay(o.id)}>
                  模拟支付
                </Button>
              ) : null}
            </View>
          ))}
        </View>
      ) : null}
    </View>
  );
}
