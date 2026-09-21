import { View, Text, Button } from "@tarojs/components";
import { useState } from "react";
import Taro, { useDidShow } from "@tarojs/taro";
import type { ProductSummary } from "@meiyue/types";
import { apiFetch, getToken } from "../../services/api";
import "./index.css";

/**
 * 浏览足迹（I30 Taro）
 *
 * 入口：我的 → 浏览足迹
 * API：GET/DELETE /api/v1/buyer/browse-history
 */
export default function BrowseHistoryPage() {
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
      const data = await apiFetch<ProductSummary[]>("/api/v1/buyer/browse-history");
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

  async function clearAll() {
    try {
      await apiFetch("/api/v1/buyer/browse-history", { method: "DELETE" });
      setList([]);
      setEmpty(true);
      Taro.showToast({ title: "已清空", icon: "success" });
    } catch (e) {
      Taro.showToast({
        title: e instanceof Error ? e.message : "清空失败",
        icon: "none"
      });
    }
  }

  return (
    <View className="page">
      <Text className="brand">美月商城</Text>
      <Text className="h1">浏览足迹</Text>
      {list.length > 0 ? (
        <Button className="btn ghost" size="mini" onClick={clearAll}>
          清空足迹
        </Button>
      ) : null}
      {error ? <Text className="err">{error}</Text> : null}
      {empty ? <Text className="muted empty">暂无足迹，打开商品详情会自动记录</Text> : null}
      {list.map((p) => (
        <View key={p.id} className="row">
          <View
            className="info"
            onClick={() => Taro.navigateTo({ url: `/pages/detail/index?id=${p.id}` })}
          >
            <Text className="title">{p.title}</Text>
            <Text className="price">¥{((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}</Text>
          </View>
        </View>
      ))}
    </View>
  );
}
