import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import { EmptyState, ErrorState } from "@meiyue/ui";
import type { ProductSummary } from "@meiyue/types";
import "./FavoritesPage.css";

/**
 * 买家收藏夹（I22）
 * 入口：顶栏「收藏」· 详情页收藏按钮
 * API：GET /buyer/favorites/products · DELETE /buyer/favorites/{id}
 */
export function FavoritesPage() {
  const navigate = useNavigate();
  const [list, setList] = useState<ProductSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function reload() {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      setList(await apiFetch<ProductSummary[]>("/api/v1/buyer/favorites/products"));
    } catch (e) {
      setError(e instanceof Error ? e.message : "加载失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload();
  }, []);

  async function remove(productId: number) {
    try {
      await apiFetch(`/api/v1/buyer/favorites/${productId}`, { method: "DELETE" });
      setList((prev) => prev.filter((p) => p.id !== productId));
    } catch (e) {
      setError(e instanceof Error ? e.message : "取消失败");
    }
  }

  return (
    <div className="my-fav my-page">
      <h1 className="my-page-title my-fade-up">我的收藏</h1>
      <p className="my-page-lead">已上架商品会显示在此 · 下架商品自动隐藏</p>
      {error ? (
        <ErrorState message={error}>
          <button type="button" className="my-btn my-btn--ghost" onClick={() => reload()}>
            重试
          </button>
        </ErrorState>
      ) : null}
      {loading ? <p className="my-muted">加载中…</p> : null}
      {!loading && list.length === 0 && !error ? (
        <EmptyState title="还没有收藏" hint="在商品详情点「收藏」即可加入">
          <Link to="/products">去逛逛</Link>
        </EmptyState>
      ) : null}
      <ul className="my-fav__list my-fade-up">
        {list.map((p) => (
          <li key={p.id}>
            <Link to={`/products/${p.id}`}>{p.title}</Link>
            <span>¥{((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}</span>
            <button type="button" className="my-btn my-btn--ghost" onClick={() => remove(p.id)}>
              取消收藏
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
