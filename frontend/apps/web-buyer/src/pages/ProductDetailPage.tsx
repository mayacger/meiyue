import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { PageShell } from "@meiyue/ui";

interface Review {
  id: number;
  rating: number;
  content: string;
  sellerReply: string | null;
  createdAt: string;
}

/** 商品详情 + 公开评价列表（I10） */
export function ProductDetailPage() {
  const { id } = useParams();
  const [data, setData] = useState<Record<string, unknown> | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      fetch(`/api/v1/products/${id}`).then((r) => r.json()),
      fetch(`/api/v1/products/${id}/reviews`).then((r) => r.json())
    ])
      .then(([prod, rev]) => {
        if (!prod.success) throw new Error(prod.message);
        setData(prod.data);
        if (rev.success) setReviews(rev.data);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"));
  }, [id]);

  return (
    <PageShell title="商品详情" subtitle={`商品 #${id}`}>
      <p><Link to="/">返回首页</Link></p>
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      {data ? <pre>{JSON.stringify(data, null, 2)}</pre> : null}
      <section>
        <h2>买家评价</h2>
        {reviews.length === 0 ? <p>暂无评价</p> : null}
        <ul>
          {reviews.map((r) => (
            <li key={r.id} style={{ marginBottom: 8 }}>
              ★{r.rating} · {r.content}
              {r.sellerReply ? <div style={{ color: "#555" }}>商家回复：{r.sellerReply}</div> : null}
              <small>{r.createdAt}</small>
            </li>
          ))}
        </ul>
      </section>
    </PageShell>
  );
}
