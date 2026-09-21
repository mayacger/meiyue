import { View, Text, Button } from "@tarojs/components";
import { useEffect, useState } from "react";
import Taro, { useRouter } from "@tarojs/taro";
import type { ProductSummary } from "@meiyue/types";
import { apiFetch, getToken } from "../../services/api";
import "./index.css";

interface Review {
  id: number;
  rating: number;
  content: string;
  sellerReply: string | null;
}

/**
 * 商品详情（I19）：加购 + 评价列表
 * API：GET /products/:id · /products/:id/reviews · POST 加购
 */
export default function DetailPage() {
  const { params } = useRouter();
  const id = params.id;
  const [product, setProduct] = useState<ProductSummary | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!id) return;
    Promise.all([
      apiFetch<ProductSummary>(`/api/v1/products/${id}`),
      apiFetch<Review[]>(`/api/v1/products/${id}/reviews`).catch(() => [] as Review[])
    ])
      .then(([p, r]) => {
        setProduct(p);
        setReviews(r);
      })
      .catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, [id]);

  async function addToCart() {
    if (!getToken()) {
      Taro.showToast({ title: "请先登录", icon: "none" });
      Taro.switchTab({ url: "/pages/mine/index" });
      return;
    }
    const skuId = product?.skus[0]?.id;
    if (!skuId) return;
    try {
      await apiFetch("/api/v1/buyer/cart/items", {
        method: "POST",
        data: { skuId, quantity: 1 }
      });
      Taro.showToast({ title: "已加购", icon: "success" });
    } catch (e) {
      Taro.showToast({
        title: e instanceof Error ? e.message : "加购失败",
        icon: "none"
      });
    }
  }

  return (
    <View className="page">
      {error ? <Text className="err">{error}</Text> : null}
      {product ? (
        <>
          <View className="hero">
            <Text className="letter">{product.title.slice(0, 1)}</Text>
          </View>
          <Text className="title">{product.title}</Text>
          <Text className="price">¥{((product.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}</Text>
          <Button className="btn" onClick={addToCart}>
            加入购物车
          </Button>
        </>
      ) : null}

      <View className="reviews">
        <Text className="h2">买家评价</Text>
        {reviews.length === 0 ? <Text className="muted">暂无评价</Text> : null}
        {reviews.map((r) => (
          <View key={r.id} className="review">
            <Text className="rating">★{r.rating}</Text>
            <Text className="content">{r.content}</Text>
            {r.sellerReply ? <Text className="muted">商家回复：{r.sellerReply}</Text> : null}
          </View>
        ))}
      </View>
    </View>
  );
}
