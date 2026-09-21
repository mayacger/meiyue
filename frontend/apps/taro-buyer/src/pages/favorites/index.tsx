import { View, Text, Button } from "@tarojs/components";
import { useState } from "react";
import Taro, { useDidShow } from "@tarojs/taro";
import type { ProductSummary } from "@meiyue/types";
import { apiFetch, getToken } from "../../services/api";
import "./index.css";

/**
 * 收藏列表（I22 Taro）
 * 入口：我的 → 我的收藏
 * API：GET /buyer/favorites/products · DELETE /buyer/favorites/{id}
 */
export default function FavoritesPage() {
  const [list, setList] = useState<ProductSummary[]>([]);
  const [error, setError] = useState("");
  const [empty, setEmpty] = useState(false);

  async function load() {
    if (!getToken()) {
      Taro.showToast({ title: "请先登录", icon: "none" });
      Taro.switchTab({ url: "/pages/mine/index" });
      return;
    }
    setError("");
    try {
      const data = await apiFetch<ProductSummary[]>("/api/v1/buyer/favorites/products");
      setList(data);
      setEmpty(data.length === 0);
    } catch (e) {
      setError(e instanceof Error ? e.message : "加载失败");
      setEmpty(false);
    }
  }

  useDidShow(() => {
    load();
  });

  async function remove(id: number) {
    try {
      await apiFetch(`/api/v1/buyer/favorites/${id}`, { method: "DELETE" });
      setList((prev) => prev.filter((p) => p.id !== id));
      Taro.showToast({ title: "已取消", icon: "success" });
    } catch (e) {
      Taro.showToast({
        title: e instanceof Error ? e.message : "失败",
        icon: "none"
      });
    }
  }

  return (
    <View className="page">
      <Text className="brand">美月商城</Text>
      <Text className="h1">我的收藏</Text>
      {error ? <Text className="err">{error}</Text> : null}
      {empty ? <Text className="muted empty">暂无收藏，去详情页点收藏吧</Text> : null}
      {list.map((p) => (
        <View key={p.id} className="row">
          <View
            className="info"
            onClick={() => Taro.navigateTo({ url: `/pages/detail/index?id=${p.id}` })}
          >
            <Text className="title">{p.title}</Text>
            <Text className="price">¥{((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}</Text>
          </View>
          <Button size="mini" className="btn ghost" onClick={() => remove(p.id)}>
            取消
          </Button>
        </View>
      ))}
    </View>
  );
}
