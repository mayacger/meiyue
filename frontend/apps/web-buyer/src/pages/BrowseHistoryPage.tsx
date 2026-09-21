import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import { EmptyState, ErrorState } from "@meiyue/ui";
import type { ProductSummary } from "@meiyue/types";
import "./FavoritesPage.css";

/**
 * 浏览足迹（I30）
 *
 * 入口：顶栏「足迹」· SiteShell → /browse-history
 * API：
 *   GET    /api/v1/buyer/browse-history
 *   DELETE /api/v1/buyer/browse-history（清空）
 * 记录：详情页登录态 POST /buyer/browse-history/{productId}
 *
 * 关系：同一商品 upsert；上限 100；已下架商品列表中跳过
 */
export function BrowseHistoryPage() {
  const navigate = useNavigate();
  const [list, setList] = useState<ProductSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);

  async function reload() {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      setList(await apiFetch<ProductSummary[]>("/api/v1/buyer/browse-history"));
    } catch (e) {
      setError(e instanceof Error ? e.message : "加载失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload();
  }, []);

  async function clearAll() {
    try {
      await apiFetch("/api/v1/buyer/browse-history", { method: "DELETE" });
      setList([]);
      setMsg("足迹已清空");
    } catch (e) {
      setError(e instanceof Error ? e.message : "清空失败");
    }
  }

  return (
    <div className="my-fav my-page">
      <h1 className="my-page-title my-fade-up">浏览足迹</h1>
      <p className="my-page-lead">
        最近浏览的上架商品 ·{" "}
        <button type="button" className="my-btn my-btn--ghost" onClick={clearAll} disabled={!list.length}>
          清空足迹
        </button>
      </p>
      {msg ? <p className="my-ok">{msg}</p> : null}
      {error ? (
        <ErrorState message={error}>
          <button type="button" className="my-btn my-btn--ghost" onClick={() => reload()}>
            重试
          </button>
        </ErrorState>
      ) : null}
      {loading ? <p className="my-muted">加载中…</p> : null}
      {!loading && list.length === 0 && !error ? (
        <EmptyState title="暂无足迹" hint="打开商品详情会自动记录">
          <Link to="/products">去逛逛</Link>
        </EmptyState>
      ) : null}
      <ul className="my-fav__list my-fade-up">
        {list.map((p) => (
          <li key={p.id}>
            <Link to={`/products/${p.id}`}>{p.title}</Link>
            <span>¥{((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}
