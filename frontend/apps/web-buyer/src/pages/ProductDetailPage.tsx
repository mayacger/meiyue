import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import type { ProductSummary } from "@meiyue/types";
import { Skeleton } from "@meiyue/ui";
import { SeoHead } from "../components/SeoHead";
import "./ProductDetailPage.css";

interface Review {
  id: number;
  rating: number;
  content: string;
  sellerReply: string | null;
}

/**
 * 商品详情（I20 + I30）
 * GET /products/:id · /products/:id/reviews · /products/:id/related
 * 登录态：POST /buyer/browse-history/{id} 记足迹 · 收藏/加购
 */
export function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState<ProductSummary | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [related, setRelated] = useState<ProductSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [favorited, setFavorited] = useState(false);
  const [galleryIdx, setGalleryIdx] = useState(0);

  useEffect(() => {
    setGalleryIdx(0);
    setLoading(true);
    setError(null);
    Promise.all([
      fetch(`/api/v1/products/${id}`).then((r) => r.json()),
      fetch(`/api/v1/products/${id}/reviews`).then((r) => r.json()),
      fetch(`/api/v1/products/${id}/related?limit=8`).then((r) => r.json())
    ])
      .then(([prod, rev, rel]) => {
        if (!prod.success) throw new Error(prod.message);
        setProduct(prod.data);
        if (rev.success) setReviews(rev.data);
        if (rel.success) setRelated(rel.data || []);
        if (getToken()) {
          // I30：记录浏览足迹（失败静默）
          apiFetch(`/api/v1/buyer/browse-history/${id}`, { method: "POST" }).catch(() => undefined);
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
  // I34：封面 + 图集去重
  const gallery = product
    ? [
        ...(product.coverImageUrl ? [product.coverImageUrl] : []),
        ...((product.galleryImageUrls || []).filter(Boolean) as string[])
      ].filter((u, i, arr) => arr.indexOf(u) === i)
    : [];
  const activeImg = gallery[galleryIdx] || null;

  return (
    <article className="my-detail">
      <SeoHead
        title={product?.title || "商品详情"}
        description={
          product
            ? `${product.title}${product.subtitle ? ` — ${product.subtitle}` : ""} · 美月商城`
            : "商品详情 · 美月商城"
        }
        ogImage={product?.coverImageUrl || gallery[0] || undefined}
        path={`/products/${id}`}
      />
      <p className="my-detail__crumb">
        <Link to="/products">全部商品</Link>
        <span aria-hidden> / </span>
        <span>详情</span>
      </p>
      {loading ? (
        <div className="my-detail__loading my-fade-up" aria-live="polite">
          <Skeleton rows={5} height={16} />
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
          <div className="my-detail__visual">
            {activeImg ? (
              <img className="my-detail__photo" src={activeImg} alt={product.title} />
            ) : (
              <>
                <span className="my-detail__letter" aria-hidden>
                  {product.title.slice(0, 1)}
                </span>
                <span className="my-detail__brand-mark">美月</span>
              </>
            )}
            {gallery.length > 1 ? (
              <div className="my-detail__thumbs" role="list">
                {gallery.map((url, i) => (
                  <button
                    key={url + i}
                    type="button"
                    className={`my-detail__thumb ${i === galleryIdx ? "is-active" : ""}`}
                    onClick={() => setGalleryIdx(i)}
                    aria-label={`图 ${i + 1}`}
                  >
                    <img src={url} alt="" />
                  </button>
                ))}
              </div>
            ) : null}
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
      {product?.promoVideoUrl ? (
        <section className="my-detail__video my-fade-up">
          <h2>推广视频</h2>
          <p className="my-muted">非直播 · AI/商家上传</p>
          <video className="my-detail__player" controls preload="metadata" src={product.promoVideoUrl}>
            您的浏览器不支持 video
          </video>
        </section>
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
      {!loading && related.length > 0 ? (
        <section className="my-detail__related my-fade-up">
          <h2>相关推荐</h2>
          <p className="my-muted">同店 / 同类目在售</p>
          <ul className="my-detail__related-list">
            {related.map((p) => (
              <li key={p.id}>
                <Link to={`/products/${p.id}`}>{p.title}</Link>
                <span>¥{((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}</span>
              </li>
            ))}
          </ul>
        </section>
      ) : null}
    </article>
  );
}
