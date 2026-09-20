import { FormEvent, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiFetch } from "@meiyue/api";
import { PageShell } from "@meiyue/ui";

interface Review {
  id: number;
  productId: number;
  orderId: number;
  rating: number;
  content: string;
  sellerReply: string | null;
  createdAt: string;
}

/** 商家：评价列表与回复 */
export function ReviewsPage() {
  const [list, setList] = useState<Review[]>([]);
  const [replyDraft, setReplyDraft] = useState<Record<number, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);

  async function reload() {
    setList(await apiFetch<Review[]>("/api/v1/seller/reviews"));
  }

  useEffect(() => {
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, []);

  async function reply(id: number, e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await apiFetch(`/api/v1/seller/reviews/${id}/reply`, {
        method: "POST",
        json: { reply: replyDraft[id] || "" }
      });
      setMsg("已回复");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "回复失败");
    }
  }

  return (
    <PageShell title="商品评价" subtitle="本店评价可见 / 回复（I10）">
      <p><Link to="/">返回概览</Link></p>
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      {msg ? <p>{msg}</p> : null}
      <ul>
        {list.map((r) => (
          <li key={r.id} style={{ marginBottom: 16, borderTop: "1px solid #ddd", paddingTop: 8 }}>
            商品#{r.productId} · 订单#{r.orderId} · ★{r.rating}
            <div>{r.content}</div>
            <small>{r.createdAt}</small>
            {r.sellerReply ? <div style={{ color: "#555" }}>已回复：{r.sellerReply}</div> : (
              <form onSubmit={(e) => reply(r.id, e)}>
                <input
                  value={replyDraft[r.id] || ""}
                  onChange={(e) => setReplyDraft({ ...replyDraft, [r.id]: e.target.value })}
                  placeholder="商家回复"
                />{" "}
                <button type="submit">回复</button>
              </form>
            )}
          </li>
        ))}
      </ul>
    </PageShell>
  );
}
