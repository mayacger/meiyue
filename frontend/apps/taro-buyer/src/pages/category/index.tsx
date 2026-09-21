import { View, Text } from "@tarojs/components";
import { useEffect, useState } from "react";
import Taro from "@tarojs/taro";
import type { ProductSummary } from "@meiyue/types";
import { apiFetch } from "../../services/api";
import "./index.css";

interface Category {
  id: number;
  name: string;
}

/**
 * 分类页（I19）：类目筛选 + 商品列表
 * API：GET /categories · GET /products?categoryId=
 */
export default function CategoryPage() {
  const [list, setList] = useState<Category[]>([]);
  const [activeId, setActiveId] = useState<number | null>(null);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [error, setError] = useState("");

  async function loadProducts(categoryId: number | null) {
    const qs = categoryId ? `?categoryId=${categoryId}` : "";
    setProducts(await apiFetch<ProductSummary[]>(`/api/v1/products${qs}`));
  }

  useEffect(() => {
    (async () => {
      try {
        const cats = await apiFetch<Category[]>("/api/v1/categories");
        setList(cats);
        await loadProducts(null);
      } catch (e) {
        setError(e instanceof Error ? e.message : "加载失败");
      }
    })();
  }, []);

  async function select(id: number | null) {
    setActiveId(id);
    setError("");
    try {
      await loadProducts(id);
    } catch (e) {
      setError(e instanceof Error ? e.message : "筛选失败");
    }
  }

  return (
    <View className="page">
      <Text className="h1">分类</Text>
      {error ? <Text className="err">{error}</Text> : null}
      <View className="chips">
        <Text className={`chip ${activeId == null ? "on" : ""}`} onClick={() => select(null)}>
          全部
        </Text>
        {list.map((c) => (
          <Text
            key={c.id}
            className={`chip ${activeId === c.id ? "on" : ""}`}
            onClick={() => select(c.id)}
          >
            {c.name}
          </Text>
        ))}
      </View>
      {products.length === 0 ? <Text className="muted">该类目暂无商品</Text> : null}
      {products.map((p) => (
        <View
          key={p.id}
          className="row"
          onClick={() => Taro.navigateTo({ url: `/pages/detail/index?id=${p.id}` })}
        >
          <Text className="title">{p.title}</Text>
          <Text className="price">¥{((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}</Text>
        </View>
      ))}
    </View>
  );
}
