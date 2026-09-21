import { View, Text, Input, Button, Image, Swiper, SwiperItem } from "@tarojs/components";
import { useEffect, useState } from "react";
import Taro from "@tarojs/taro";
import type { ProductSummary } from "@meiyue/types";
import { apiFetch, getToken } from "../../services/api";
import "./index.css";

/** 平台 Banner（I28） */
interface Banner {
  id: number;
  title: string;
  imageUrl: string;
  linkUrl: string | null;
}

const LOCAL_KEY = "meiyue_search_history";

function readLocal(): string[] {
  try {
    const raw = Taro.getStorageSync(LOCAL_KEY);
    if (!raw) return [];
    const arr = typeof raw === "string" ? (JSON.parse(raw) as string[]) : (raw as string[]);
    return Array.isArray(arr) ? arr.slice(0, 20) : [];
  } catch {
    return [];
  }
}

function writeLocal(list: string[]) {
  Taro.setStorageSync(LOCAL_KEY, JSON.stringify(list.slice(0, 20)));
}

/**
 * 首页（I19 + I28 Banner + I32 搜索历史）
 * 登录记服务端历史；未登录用本地 Storage
 */
export default function IndexPage() {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [banners, setBanners] = useState<Banner[]>([]);
  const [history, setHistory] = useState<string[]>([]);
  const [q, setQ] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  async function loadHistory() {
    if (getToken()) {
      try {
        setHistory(await apiFetch<string[]>("/api/v1/buyer/search-history"));
        return;
      } catch {
        /* local */
      }
    }
    setHistory(readLocal());
  }

  async function recordKeyword(keyword: string) {
    const k = keyword.trim();
    if (!k) return;
    if (getToken()) {
      try {
        await apiFetch("/api/v1/buyer/search-history", { method: "POST", data: { keyword: k } });
        await loadHistory();
        return;
      } catch {
        /* local */
      }
    }
    const next = [k, ...readLocal().filter((x) => x !== k)].slice(0, 20);
    writeLocal(next);
    setHistory(next);
  }

  async function clearHistory() {
    if (getToken()) {
      try {
        await apiFetch("/api/v1/buyer/search-history", { method: "DELETE" });
      } catch {
        /* ignore */
      }
    }
    writeLocal([]);
    setHistory([]);
  }

  async function load(keyword = "") {
    const qs = keyword.trim() ? `?q=${encodeURIComponent(keyword.trim())}` : "";
    setProducts(await apiFetch<ProductSummary[]>(`/api/v1/products${qs}`));
  }

  useEffect(() => {
    setLoading(true);
    Promise.all([
      apiFetch<Banner[]>("/api/v1/banners").catch(() => [] as Banner[]),
      load(),
      loadHistory()
    ])
      .then(([b]) => setBanners(b || []))
      .catch((e) => setError(e instanceof Error ? e.message : "加载失败"))
      .finally(() => setLoading(false));
  }, []);

  async function onSearch(keyword = q) {
    setError("");
    try {
      await recordKeyword(keyword);
      setQ(keyword);
      await load(keyword);
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
            onConfirm={() => onSearch()}
          />
          <Button className="search-btn" size="mini" onClick={() => onSearch()}>
            搜索
          </Button>
        </View>
        {history.length > 0 ? (
          <View className="history">
            {history.slice(0, 8).map((h) => (
              <Text key={h} className="history-chip" onClick={() => onSearch(h)}>
                {h}
              </Text>
            ))}
            <Text className="history-clear" onClick={() => clearHistory()}>
              清空
            </Text>
          </View>
        ) : null}
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
            className="card"
            onClick={() => Taro.navigateTo({ url: `/pages/detail/index?id=${p.id}` })}
          >
            <Text className="title">{p.title}</Text>
            <Text className="price">¥{((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}</Text>
          </View>
        ))}
      </View>
    </View>
  );
}
