import { View, Text, Button } from "@tarojs/components";
import { useState } from "react";
import Taro, { useDidShow } from "@tarojs/taro";
import type { CouponClaim, CouponSummary } from "@meiyue/types";
import { apiFetch, getToken } from "../../services/api";
import "./index.css";

/**
 * 领券中心（I23 Taro）
 * 入口：我的 → 领券
 * API：GET /platform-coupons · POST /buyer/platform-coupons/{id}/claim
 */
export default function CouponsPage() {
  const [list, setList] = useState<CouponSummary[]>([]);
  const [claims, setClaims] = useState<CouponClaim[]>([]);
  const [error, setError] = useState("");

  async function load() {
    setError("");
    try {
      setList(await apiFetch<CouponSummary[]>("/api/v1/platform-coupons"));
      if (getToken()) {
        setClaims(await apiFetch<CouponClaim[]>("/api/v1/buyer/platform-coupons/claims"));
      } else {
        setClaims([]);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : "加载失败");
    }
  }

  useDidShow(() => {
    load();
  });

  async function claim(id: number) {
    if (!getToken()) {
      Taro.showToast({ title: "请先登录", icon: "none" });
      Taro.switchTab({ url: "/pages/mine/index" });
      return;
    }
    try {
      await apiFetch(`/api/v1/buyer/platform-coupons/${id}/claim`, { method: "POST" });
      Taro.showToast({ title: "领取成功", icon: "success" });
      await load();
    } catch (e) {
      Taro.showToast({
        title: e instanceof Error ? e.message : "领取失败",
        icon: "none"
      });
    }
  }

  const claimed = new Set(claims.map((c) => c.couponId));

  return (
    <View className="page">
      <Text className="brand">美月商城</Text>
      <Text className="h1">领券中心</Text>
      <Text className="lead">平台券一键领取 · 下单结算可用</Text>
      {error ? <Text className="err">{error}</Text> : null}
      {list.length === 0 && !error ? <Text className="muted empty">暂无可领平台券</Text> : null}
      {list.map((c) => {
        const done = claimed.has(c.id);
        return (
          <View key={c.id} className="card">
            <View>
              <Text className="title">{c.title}</Text>
              <Text className="muted">
                满{(c.minSpendCents / 100).toFixed(0)}减{(c.discountCents / 100).toFixed(0)} ·{" "}
                {c.code}
              </Text>
            </View>
            <Button
              size="mini"
              className="btn"
              disabled={done}
              onClick={() => claim(c.id)}
            >
              {done ? "已领" : "领取"}
            </Button>
          </View>
        );
      })}
    </View>
  );
}
