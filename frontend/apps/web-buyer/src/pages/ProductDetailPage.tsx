import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import type { ProductSummary } from "@meiyue/types";
import "./ProductDetailPage.css";

interface Review {
  id: number;
  rating: number;
  content: string;
  sellerReply: string | null;
}

/**
 * 商品详情（I20 视觉打磨）
 * GET /products/:id · /products/:id/reviews · POST 加购
 * 加载态 / 空评价 / 品牌排版
 */
export function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState<ProductSummary | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [favorited, setFavorited] = useState(false);

  useEffect(() => {
    setLoading(true);
    setError(null);
    Promise.all([
      fetch(`/api/v1/products/${id}`).then((r) => r.json()),
      fetch(`/api/v1/products/${id}/reviews`).then((r) => r.json())
    ])
      .then(([prod, rev]) => {
        if (!prod.success) throw new Error(prod.message);
        setProduct(prod.data);
        if (rev.success) setReviews(rev.data);
        if (getToken()) {
          apiFetch<{ favorited: boolean }>(`/api/v1/buyer/favorites/${id}/status`)
            .then((s) => setFavorited(s.favorited))
            .catch(() => setFavorited(false));
        }
      })
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"))
      .finally(() => setLoading(false));
  }, [id]);

  async function addToCart() {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    const skuId = product?.skus[0]?.id;
    if (!skuId) return;
    setError(null);
    try {
      await apiFetch("/api/v1/buyer/cart/items", {
        method: "POST",
        json: { skuId, quantity: 1 }
      });
      setMsg("已加入购物车");
    } catch (err) {
      setError(err instanceof Error ? err.message : "加购失败");
    }
  }

  async function toggleFavorite() {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    if (!id) return;
    setError(null);
    try {
      if (favorited) {
        await apiFetch(`/api/v1/buyer/favorites/${id}`, { method: "DELETE" });
        setFavorited(false);
        setMsg("已取消收藏");
      } else {
        await apiFetch(`/api/v1/buyer/favorites/${id}`, { method: "POST" });
        setFavorited(true);
        setMsg("已加入收藏");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "收藏失败");
    }
  }

  const price = ((product?.skus[0]?.priceCents ?? 0) / 100).toFixed(2);

  return (
    <article className="my-detail">
      <p className="my-detail__crumb">
        <Link to="/products">全部商品</Link>
        <span aria-hidden> / </span>
        <span>详情</span>
      </p>
      {loading ? (
        <div className="my-detail__loading my-fade-up" aria-live="polite">
          <span className="my-state__pulse" />
          正在展开商品…
        </div>
      ) : null}
      {error ? <p className="my-error">{error}</p> : null}
      {msg ? <p className="my-ok">{msg}</p> : null}
      {!loading && !product && !error ? (
        <div className="my-empty">
          商品不存在或已下架 · <Link to="/products">返回列表</Link>
        </div>
      ) : null}
      {product ? (
        <div className="my-detail__grid my-fade-up">
          <div className="my-detail__visual" aria-hidden>
            <span className="my-detail__letter">{product.title.slice(0, 1)}</span>
            <span className="my-detail__brand-mark">美月</span>
          </div>
          <div className="my-detail__info">
            <p className="my-detail__eyebrow">meiyuemall</p>
            <h1>{product.title}</h1>
            <p className="my-detail__price">¥{price}</p>
            <p className="my-detail__sub">{product.subtitle || "精选好物 · 美月履约"}</p>
            <div className="my-detail__actions">
              <button type="button" className="my-btn my-btn--primary" onClick={addToCart}>
                加入购物车
              </button>
              <button type="button" className="my-btn my-btn--ghost" onClick={toggleFavorite}>
                {favorited ? "已收藏" : "收藏"}
              </button>
              <Link to={`/stores/${product.tenantId}`} className="my-detail__cart-link">
                进店逛逛
              </Link>
              <Link to="/cart" className="my-detail__cart-link">
                查看购物车
              </Link>
            </div>
          </div>
        </div>
      ) : null}
      {!loading ? (
        <section className="my-detail__reviews my-fade-up">
          <h2>买家评价{reviews.length > 0 ? ` · ${reviews.length}` : ""}</h2>
          {reviews.length === 0 ? (
            <p className="my-muted my-detail__empty-reviews">暂无评价，确认收货后可在订单详情提交</p>
          ) : null}
          <ul>
            {reviews.map((r) => (
              <li key={r.id}>
                <strong>★{r.rating}</strong>
                <span>{r.content}</span>
                {r.sellerReply ? <div className="my-muted">商家回复：{r.sellerReply}</div> : null}
              </li>
            ))}
          </ul>
        </section>
      ) : null}
    </article>
  );
}
