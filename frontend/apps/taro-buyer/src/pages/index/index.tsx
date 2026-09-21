import { View, Text } from "@tarojs/components";
import { useEffect, useState } from "react";
import Taro from "@tarojs/taro";
import type { ProductSummary } from "@meiyue/types";
import { apiFetch } from "../../services/api";
import "./index.css";

/**
 * 首页：品牌标题 + 商品列表（可点进详情）
 * API：GET /api/v1/products
 */
export default function IndexPage() {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    apiFetch<ProductSummary[]>("/api/v1/products")
      .then(setProducts)
      .catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, []);

  return (
    <View className="page">
      <View className="hero">
        <Text className="brand">美月商城</Text>
        <Text className="lead">月色下的好店与好物</Text>
      </View>
      {error ? <Text className="err">{error}</Text> : null}
      <View className="list">
        {products.map((p) => (
          <View
            key={p.id}
            className="item"
            onClick={() =>
              Taro.navigateTo({ url: `/pages/detail/index?id=${p.id}` })
            }
          >
            <View className="thumb">
              <Text>{p.title.slice(0, 1)}</Text>
            </View>
            <View className="meta">
              <Text className="title">{p.title}</Text>
              <Text className="price">
                ¥{((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}
              </Text>
            </View>
          </View>
        ))}
      </View>
    </View>
  );
}
