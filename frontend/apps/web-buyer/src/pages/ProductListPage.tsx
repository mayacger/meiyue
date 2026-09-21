import { FormEvent, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import type { ProductSummary } from "@meiyue/types";
import { Skeleton } from "@meiyue/ui";
import { ProductRail } from "../components/ProductRail";
import { SeoHead } from "../components/SeoHead";
import "./ProductListPage.css";

interface Category {
  id: number;
  name: string;
}

const LOCAL_SEARCH_KEY = "meiyue_search_history";

/** 本地最近关键词（未登录兜底） */
function readLocalHistory(): string[] {
  try {
    const raw = localStorage.getItem(LOCAL_SEARCH_KEY);
    if (!raw) return [];
    const arr = JSON.parse(raw) as string[];
    return Array.isArray(arr) ? arr.slice(0, 20) : [];
  } catch {
    return [];
  }
}

function writeLocalHistory(list: string[]) {
  localStorage.setItem(LOCAL_SEARCH_KEY, JSON.stringify(list.slice(0, 20)));
}

/**
 * 商品列表 / 搜索（I19 + I32 搜索历史）
 * API：GET /products?q= · POST/GET/DELETE /buyer/search-history
 * 登录：服务端历史；未登录：localStorage
 */
export function ProductListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [q, setQ] = useState(searchParams.get("q") || "");
  const [categoryId, setCategoryId] = useState<number | null>(
    searchParams.get("categoryId") ? Number(searchParams.get("categoryId")) : null
  );
  const [history, setHistory] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  async function loadHistory() {
    if (getToken()) {
      try {
        setHistory(await apiFetch<string[]>("/api/v1/buyer/search-history"));
        return;
      } catch {
        /* 降级本地 */
      }
    }
    setHistory(readLocalHistory());
  }

  async function recordKeyword(keyword: string) {
    const k = keyword.trim();
    if (!k) return;
    if (getToken()) {
      try {
        await apiFetch("/api/v1/buyer/search-history", { method: "POST", json: { keyword: k } });
        await loadHistory();
        return;
      } catch {
        /* 降级本地 */
      }
    }
    const next = [k, ...readLocalHistory().filter((x) => x !== k)].slice(0, 20);
    writeLocalHistory(next);
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
    writeLocalHistory([]);
    setHistory([]);
  }

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
    loadHistory();
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

  async function runSearch(keyword: string) {
    setError(null);
    setQ(keyword);
    syncUrl(keyword, categoryId);
    await recordKeyword(keyword);
    await load(keyword, categoryId);
  }

  function onSearch(e: FormEvent) {
    e.preventDefault();
    runSearch(q).catch((err) => setError(err instanceof Error ? err.message : "搜索失败"));
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
        {history.length > 0 ? (
          <div className="my-list__history">
            <span className="my-muted">最近搜索</span>
            {history.map((h) => (
              <button
                key={h}
                type="button"
                className="my-chip"
                onClick={() =>
                  runSearch(h).catch((err) =>
                    setError(err instanceof Error ? err.message : "搜索失败")
                  )
                }
              >
                {h}
              </button>
            ))}
            <button type="button" className="my-btn my-btn--ghost" onClick={() => clearHistory()}>
              清空
            </button>
          </div>
        ) : null}
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
