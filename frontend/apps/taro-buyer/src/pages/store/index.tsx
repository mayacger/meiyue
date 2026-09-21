import { View, Text, Button, Image } from "@tarojs/components";
import { useEffect, useState } from "react";
import Taro, { useRouter } from "@tarojs/taro";
import type { ProductSummary, StoreInfo } from "@meiyue/types";
import { apiFetch } from "../../services/api";
import "./index.css";

/**
 * 店铺页（I22 Taro）
 * 入口：详情「进店」· /pages/store/index?tenantId=
 * API：GET /stores/{tenantId} · /stores/{tenantId}/products
 */
export default function StorePage() {
  const { params } = useRouter();
  const tenantId = params.tenantId;
  const [store, setStore] = useState<StoreInfo | null>(null);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!tenantId) return;
    Promise.all([
      apiFetch<StoreInfo>(`/api/v1/stores/${tenantId}`),
      apiFetch<ProductSummary[]>(`/api/v1/stores/${tenantId}/products`)
    ])
      .then(([s, p]) => {
        setStore(s);
        setProducts(p);
      })
      .catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, [tenantId]);

  return (
    <View className="page">
      {error ? <Text className="err">{error}</Text> : null}
      {store ? (
        <View className="hero">
          {store.logoUrl ? (
            <Image className="logo" src={store.logoUrl} mode="aspectFill" />
          ) : (
            <Text className="mark">{store.name.slice(0, 1)}</Text>
          )}
          <View className="meta">
            <Text className="brand">美月商城 · 店铺</Text>
            <Text className="name">{store.name}</Text>
            <Text className="desc">{store.description || "暂无简介"}</Text>
          </View>
        </View>
      ) : null}
      <Text className="h2">在售商品</Text>
      {products.length === 0 && !error ? (
        <Text className="muted empty">暂无在售商品</Text>
      ) : null}
      {products.map((p) => (
        <View
          key={p.id}
          className="goods"
          onClick={() => Taro.navigateTo({ url: `/pages/detail/index?id=${p.id}` })}
        >
          <Text className="title">{p.title}</Text>
          <Text className="price">¥{((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}</Text>
        </View>
      ))}
      <Button className="btn" onClick={() => Taro.switchTab({ url: "/pages/index/index" })}>
        回首页
      </Button>
    </View>
  );
}
