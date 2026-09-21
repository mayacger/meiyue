import { View, Text, Button, Image, Video, Swiper, SwiperItem } from "@tarojs/components";
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
 * 商品详情（I19 + I22 + I30 + I34 图集/推广视频）
 */
export default function DetailPage() {
  const { params } = useRouter();
  const id = params.id;
  const [product, setProduct] = useState<ProductSummary | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [related, setRelated] = useState<ProductSummary[]>([]);
  const [favorited, setFavorited] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!id) return;
    Promise.all([
      apiFetch<ProductSummary>(`/api/v1/products/${id}`),
      apiFetch<Review[]>(`/api/v1/products/${id}/reviews`).catch(() => [] as Review[]),
      apiFetch<ProductSummary[]>(`/api/v1/products/${id}/related?limit=6`).catch(
        () => [] as ProductSummary[]
      )
    ])
      .then(([p, r, rel]) => {
        setProduct(p);
        setReviews(r);
        setRelated(rel);
        if (getToken()) {
          apiFetch(`/api/v1/buyer/browse-history/${id}`, { method: "POST" }).catch(() => undefined);
          apiFetch<{ favorited: boolean }>(`/api/v1/buyer/favorites/${id}/status`)
            .then((s) => setFavorited(s.favorited))
            .catch(() => setFavorited(false));
        }
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

  async function toggleFavorite() {
    if (!getToken()) {
      Taro.showToast({ title: "请先登录", icon: "none" });
      Taro.switchTab({ url: "/pages/mine/index" });
      return;
    }
    if (!id) return;
    try {
      if (favorited) {
        await apiFetch(`/api/v1/buyer/favorites/${id}`, { method: "DELETE" });
        setFavorited(false);
        Taro.showToast({ title: "已取消收藏", icon: "success" });
      } else {
        await apiFetch(`/api/v1/buyer/favorites/${id}`, { method: "POST" });
        setFavorited(true);
        Taro.showToast({ title: "已收藏", icon: "success" });
      }
    } catch (e) {
      Taro.showToast({
        title: e instanceof Error ? e.message : "操作失败",
        icon: "none"
      });
    }
  }

  const gallery = product
    ? [
        ...(product.coverImageUrl ? [product.coverImageUrl] : []),
        ...((product.galleryImageUrls || []).filter(Boolean) as string[])
      ].filter((u, i, arr) => arr.indexOf(u) === i)
    : [];

  return (
    <View className="page">
      {error ? <Text className="err">{error}</Text> : null}
      {!product && !error ? <Text className="muted empty">商品不存在或已下架</Text> : null}
      {product ? (
        <>
          {gallery.length > 0 ? (
            <Swiper className="gallery" circular indicatorDots autoplay={false}>
              {gallery.map((url) => (
                <SwiperItem key={url}>
                  <Image className="gallery-img" src={url} mode="aspectFill" />
                </SwiperItem>
              ))}
            </Swiper>
          ) : (
            <View className="hero">
              <Text className="letter">{product.title.slice(0, 1)}</Text>
            </View>
          )}
          <Text className="title">{product.title}</Text>
          <Text className="price">¥{((product.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}</Text>
          <View className="actions">
            <Button className="btn" onClick={addToCart}>
              加入购物车
            </Button>
            <Button className="btn ghost" onClick={toggleFavorite}>
              {favorited ? "已收藏" : "收藏"}
            </Button>
            <Button
              className="btn ghost"
              onClick={() =>
                Taro.navigateTo({ url: `/pages/store/index?tenantId=${product.tenantId}` })
              }
            >
              进店
            </Button>
          </View>
          {product.promoVideoUrl ? (
            <View className="video-wrap">
              <Text className="h2">推广视频</Text>
              <Video className="video" src={product.promoVideoUrl} controls showCenterPlayBtn />
            </View>
          ) : null}
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

      {related.length > 0 ? (
        <View className="related">
          <Text className="h2">相关推荐</Text>
          {related.map((p) => (
            <View
              key={p.id}
              className="related-row"
              onClick={() => Taro.navigateTo({ url: `/pages/detail/index?id=${p.id}` })}
            >
              <Text className="related-title">{p.title}</Text>
              <Text className="price">¥{((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}</Text>
            </View>
          ))}
        </View>
      ) : null}
    </View>
  );
}
