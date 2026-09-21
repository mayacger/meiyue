import { View, Text, Input, Button, Image, Swiper, SwiperItem } from "@tarojs/components";
import { useEffect, useState } from "react";
import Taro from "@tarojs/taro";
import type { ProductSummary } from "@meiyue/types";
import { apiFetch } from "../../services/api";
import "./index.css";

/** 平台 Banner（I28） */
interface Banner {
  id: number;
  title: string;
  imageUrl: string;
  linkUrl: string | null;
}

/**
 * 首页（I19 + I28 Banner）
 * API：GET /api/v1/banners · GET /api/v1/products?q=
 * 禁直播组件
 */
export default function IndexPage() {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [banners, setBanners] = useState<Banner[]>([]);
  const [q, setQ] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  async function load(keyword = "") {
    const qs = keyword.trim() ? `?q=${encodeURIComponent(keyword.trim())}` : "";
    setProducts(await apiFetch<ProductSummary[]>(`/api/v1/products${qs}`));
  }

  useEffect(() => {
    setLoading(true);
    Promise.all([
      apiFetch<Banner[]>("/api/v1/banners").catch(() => [] as Banner[]),
      load()
    ])
      .then(([b]) => setBanners(b || []))
      .catch((e) => setError(e instanceof Error ? e.message : "加载失败"))
      .finally(() => setLoading(false));
  }, []);

  async function onSearch() {
    setError("");
    try {
      await load(q);
    } catch (e) {
      setError(e instanceof Error ? e.message : "搜索失败");
    }
  }

  function onBannerTap(b: Banner) {
    const href = b.linkUrl || "";
    if (href.startsWith("/products")) {
      Taro.navigateTo({ url: "/pages/category/index" }).catch(() => undefined);
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

      {banners.length > 0 ? (
        <View className="banner-wrap">
          <Swiper className="banner-swiper" circular autoplay indicatorDots>
            {banners.map((b) => (
              <SwiperItem key={b.id}>
                <View className="banner-item" onClick={() => onBannerTap(b)}>
                  <Image className="banner-img" src={b.imageUrl} mode="aspectFill" />
                  <Text className="banner-cap">{b.title}</Text>
                </View>
              </SwiperItem>
            ))}
          </Swiper>
        </View>
      ) : null}

      {error ? <Text className="err">{error}</Text> : null}
      {loading ? <Text className="empty">加载中…</Text> : null}
      <View className="list">
        {!loading && products.length === 0 ? <Text className="empty">暂无商品</Text> : null}
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
