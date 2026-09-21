import { FormEvent, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import type { ProductSummary } from "@meiyue/types";
import { Skeleton } from "@meiyue/ui";
import { ProductRail } from "../components/ProductRail";
import { SeoHead } from "../components/SeoHead";
import "./ProductListPage.css";

interface Category {
  id: number;
  name: string;
}

/**
 * 商品列表 / 搜索（I19 打磨）
 * API：GET /products?q=&categoryId=
 * 支持类目芯片筛选 + URL 同步
 */
export function ProductListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [q, setQ] = useState(searchParams.get("q") || "");
  const [categoryId, setCategoryId] = useState<number | null>(
    searchParams.get("categoryId") ? Number(searchParams.get("categoryId")) : null
  );
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  async function load(keyword = q, cat = categoryId) {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (keyword.trim()) params.set("q", keyword.trim());
      if (cat) params.set("categoryId", String(cat));
      const qs = params.toString();
      const res = await fetch(`/api/v1/products${qs ? `?${qs}` : ""}`);
      const body = await res.json();
      if (!body.success) throw new Error(body.message || "加载失败");
      setProducts(body.data);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetch("/api/v1/categories")
      .then((r) => r.json())
      .then((body) => {
        if (body.success) setCategories(body.data);
      })
      .catch(() => undefined);
    const initialQ = searchParams.get("q") || "";
    const initialCat = searchParams.get("categoryId")
      ? Number(searchParams.get("categoryId"))
      : null;
    load(initialQ, initialCat).catch((e) =>
      setError(e instanceof Error ? e.message : "加载失败")
    );
  }, []);

  function syncUrl(keyword: string, cat: number | null) {
    const next = new URLSearchParams();
    if (keyword.trim()) next.set("q", keyword.trim());
    if (cat) next.set("categoryId", String(cat));
    setSearchParams(next, { replace: true });
  }

  function onSearch(e: FormEvent) {
    e.preventDefault();
    setError(null);
    syncUrl(q, categoryId);
    load(q, categoryId).catch((err) => setError(err instanceof Error ? err.message : "搜索失败"));
  }

  function selectCategory(id: number | null) {
    setCategoryId(id);
    setError(null);
    syncUrl(q, id);
    load(q, id).catch((err) => setError(err instanceof Error ? err.message : "筛选失败"));
  }

  return (
    <div className="my-list">
      <SeoHead title="全部商品" description="浏览美月商城在售商品，多商家精选上架。" path="/products" />
      <header className="my-list__head my-fade-up">
        <h1>全部商品</h1>
        <form onSubmit={onSearch} className="my-list__search">
          <input
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="搜索标题或类目关键词"
            aria-label="搜索"
          />
          <button type="submit" className="my-btn my-btn--primary">
            搜索
          </button>
        </form>
        <div className="my-list__chips" role="list">
          <button
            type="button"
            className={`my-chip ${categoryId == null ? "is-active" : ""}`}
            onClick={() => selectCategory(null)}
          >
            全部
          </button>
          {categories.map((c) => (
            <button
              key={c.id}
              type="button"
              className={`my-chip ${categoryId === c.id ? "is-active" : ""}`}
              onClick={() => selectCategory(c.id)}
            >
              {c.name}
            </button>
          ))}
        </div>
        {error ? <p className="my-error">{error}</p> : null}
        <p className="my-muted my-list__count">共 {products.length} 件</p>
      </header>
      {loading ? (
        <Skeleton rows={4} />
      ) : products.length === 0 ? (
        <div className="my-empty">没有匹配商品，试试其它关键词或类目</div>
      ) : (
        <ProductRail products={products} />
      )}
    </div>
  );
}
