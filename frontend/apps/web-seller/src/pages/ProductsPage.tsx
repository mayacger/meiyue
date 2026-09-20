import { FormEvent, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiFetch } from "@meiyue/api";
import { PageShell } from "@meiyue/ui";

interface Category {
  id: number;
  name: string;
}
interface Product {
  id: number;
  title: string;
  status: string;
  promoVideoUrl?: string | null;
  skus: { skuCode: string; priceCents: number; stockQty: number }[];
}

interface VideoTask {
  id: number;
  status: string;
  prompt: string;
  resultUrl: string | null;
  productId: number | null;
}

/** 商家商品管理 + AI 推广视频 MOCK（I2/I11） */
export function ProductsPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [list, setList] = useState<Product[]>([]);
  const [videos, setVideos] = useState<VideoTask[]>([]);
  const [title, setTitle] = useState("");
  const [categoryId, setCategoryId] = useState<number | "">("");
  const [skuCode, setSkuCode] = useState("");
  const [priceYuan, setPriceYuan] = useState("99");
  const [stock, setStock] = useState("10");
  const [videoPrompt, setVideoPrompt] = useState("商品展示短视频");
  const [videoProductId, setVideoProductId] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);

  async function reload() {
    const products = await apiFetch<Product[]>("/api/v1/seller/products");
    setList(products);
    setVideos(await apiFetch<VideoTask[]>("/api/v1/seller/ai/videos"));
  }

  useEffect(() => {
    apiFetch<Category[]>("/api/v1/categories").then(setCategories).catch(() => undefined);
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, []);

  async function onCreate(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await apiFetch("/api/v1/seller/products", {
        method: "POST",
        json: {
          categoryId: categoryId === "" ? null : categoryId,
          title,
          subtitle: "",
          detailHtml: "",
          skus: [
            {
              skuCode,
              specText: "默认",
              priceCents: Math.round(parseFloat(priceYuan) * 100),
              stockQty: parseInt(stock, 10)
            }
          ]
        }
      });
      setTitle("");
      setSkuCode("");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "创建失败");
    }
  }

  async function setStatus(id: number, status: string) {
    await apiFetch(`/api/v1/seller/products/${id}/status`, {
      method: "POST",
      json: { status }
    });
    await reload();
  }

  async function submitVideo(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await apiFetch("/api/v1/seller/ai/videos", {
        method: "POST",
        json: {
          prompt: videoPrompt,
          productId: videoProductId ? Number(videoProductId) : null
        }
      });
      setMsg("视频任务已提交（MOCK 异步），稍后刷新查看");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "提交失败");
    }
  }

  return (
    <PageShell title="商品管理" subtitle="SPU/SKU · AI 推广视频 MOCK（非直播，I2/I11）">
      <p>
        <Link to="/">返回概览</Link> · <Link to="/decoration">店铺装修</Link>
      </p>
      <form onSubmit={onCreate} style={{ display: "grid", gap: 8, maxWidth: 420 }}>
        <label>
          类目
          <select value={categoryId} onChange={(e) => setCategoryId(e.target.value ? Number(e.target.value) : "")} required>
            <option value="">请选择</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          标题
          <input value={title} onChange={(e) => setTitle(e.target.value)} required />
        </label>
        <label>
          SKU 编码
          <input value={skuCode} onChange={(e) => setSkuCode(e.target.value)} required />
        </label>
        <label>
          价格（元）
          <input value={priceYuan} onChange={(e) => setPriceYuan(e.target.value)} required />
        </label>
        <label>
          库存
          <input value={stock} onChange={(e) => setStock(e.target.value)} required />
        </label>
        {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
        {msg ? <p>{msg}</p> : null}
        <button type="submit">创建草稿商品</button>
      </form>

      <h2>AI 推广视频（MOCK）</h2>
      <form onSubmit={submitVideo}>
        <input value={videoPrompt} onChange={(e) => setVideoPrompt(e.target.value)} placeholder="提示词" />{" "}
        <select value={videoProductId} onChange={(e) => setVideoProductId(e.target.value)}>
          <option value="">不挂商品</option>
          {list.map((p) => <option key={p.id} value={p.id}>#{p.id} {p.title}</option>)}
        </select>{" "}
        <button type="submit">提交任务</button>{" "}
        <button type="button" onClick={() => reload().catch(() => undefined)}>刷新任务</button>
      </form>
      <ul>
        {videos.map((v) => (
          <li key={v.id}>
            任务#{v.id} [{v.status}] {v.prompt}
            {v.resultUrl ? ` → ${v.resultUrl}` : ""}
            {v.productId ? ` · 商品#${v.productId}` : ""}
          </li>
        ))}
      </ul>

      <h2>我的商品</h2>
      <ul>
        {list.map((p) => (
          <li key={p.id}>
            #{p.id} {p.title} [{p.status}] ¥
            {((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}
            {p.promoVideoUrl ? ` · 视频 ${p.promoVideoUrl}` : ""}
            {" "}
            <button type="button" onClick={() => setStatus(p.id, "ON_SALE")}>
              上架
            </button>{" "}
            <button type="button" onClick={() => setStatus(p.id, "OFF_SALE")}>
              下架
            </button>
          </li>
        ))}
      </ul>
    </PageShell>
  );
}
