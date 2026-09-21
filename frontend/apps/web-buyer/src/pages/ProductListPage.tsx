import { FormEvent, useEffect, useState } from "react";
import type { ProductSummary } from "@meiyue/types";
import { ProductRail } from "../components/ProductRail";
import "./ProductListPage.css";

/**
 * 商品列表 / 搜索
 * API：GET /products?q=&categoryId=
 */
export function ProductListPage() {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [q, setQ] = useState("");
  const [error, setError] = useState<string | null>(null);

  async function load(keyword = q) {
    const params = new URLSearchParams();
    if (keyword.trim()) params.set("q", keyword.trim());
    const qs = params.toString();
    const res = await fetch(`/api/v1/products${qs ? `?${qs}` : ""}`);
    const body = await res.json();
    if (!body.success) throw new Error(body.message || "加载失败");
    setProducts(body.data);
  }

  useEffect(() => {
    load("").catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, []);

  function onSearch(e: FormEvent) {
    e.preventDefault();
    setError(null);
    load().catch((err) => setError(err instanceof Error ? err.message : "搜索失败"));
  }

  return (
    <div className="my-list">
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
        {error ? <p className="my-error">{error}</p> : null}
      </header>
      <ProductRail products={products} />
    </div>
  );
}
