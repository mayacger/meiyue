import { View, Text, Input, Button } from "@tarojs/components";
import { useEffect, useState } from "react";
import Taro from "@tarojs/taro";
import type { ProductSummary } from "@meiyue/types";
import { apiFetch } from "../../services/api";
import "./index.css";

/**
 * 首页（I19）：品牌 + 搜索 + 商品列表
 * API：GET /api/v1/products?q=
 */
export default function IndexPage() {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [q, setQ] = useState("");
  const [error, setError] = useState("");

  async function load(keyword = "") {
    const qs = keyword.trim() ? `?q=${encodeURIComponent(keyword.trim())}` : "";
    setProducts(await apiFetch<ProductSummary[]>(`/api/v1/products${qs}`));
  }

  useEffect(() => {
    load().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, []);

  async function onSearch() {
    setError("");
    try {
      await load(q);
    } catch (e) {
      setError(e instanceof Error ? e.message : "搜索失败");
    }
  }

  return (
    <View className="page">
      <View className="hero">
        <Text className="brand">美月商城</Text>
        <Text className="lead">月色下的好店与好物</Text>
        <View className="search">
          <Input
            className="search-input"
            placeholder="搜索商品"
            value={q}
            onInput={(e) => setQ(e.detail.value)}
            confirmType="search"
            onConfirm={onSearch}
          />
          <Button className="search-btn" size="mini" onClick={onSearch}>
            搜索
          </Button>
        </View>
      </View>
      {error ? <Text className="err">{error}</Text> : null}
      <View className="list">
        {products.length === 0 ? <Text className="empty">暂无商品</Text> : null}
        {products.map((p) => (
          <View
            key={p.id}
            className="item"
            onClick={() => Taro.navigateTo({ url: `/pages/detail/index?id=${p.id}` })}
          >
            <View className="thumb">
              <Text>{p.title.slice(0, 1)}</Text>
            </View>
            <View className="meta">
              <Text className="title">{p.title}</Text>
              <Text className="price">¥{((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}</Text>
            </View>
          </View>
        ))}
      </View>
    </View>
  );
}
