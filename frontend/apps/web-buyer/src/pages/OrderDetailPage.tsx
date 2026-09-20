import { FormEvent, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import { PageShell } from "@meiyue/ui";

interface Track {
  status: string;
  description: string;
  trackedAt: string;
}

interface Shipment {
  id: number;
  direction: string;
  status: string;
  carrierCode: string;
  trackingNo: string;
  tracks: Track[];
}

interface Aftersale {
  id: number;
  aftersaleNo: string;
  orderId: number;
  type: string;
  status: string;
  refundCents: number;
  reverseShipmentId: number | null;
}

interface OrderItem {
  id: number;
  productId: number;
  productTitle: string;
}

interface Order {
  id: number;
  orderNo: string;
  status: string;
  items: OrderItem[];
}

/** 买家：确认收货 / 评价 / 物流 / 售后 */
export function OrderDetailPage() {
  const { id } = useParams();
  const orderId = Number(id);
  const [order, setOrder] = useState<Order | null>(null);
  const [shipments, setShipments] = useState<Shipment[]>([]);
  const [aftersales, setAftersales] = useState<Aftersale[]>([]);
  const [type, setType] = useState<"REFUND_ONLY" | "RETURN_REFUND">("REFUND_ONLY");
  const [reason, setReason] = useState("不想要了");
  const [refundYuan, setRefundYuan] = useState("99");
  const [carrier, setCarrier] = useState("SF");
  const [tracking, setTracking] = useState("");
  const [rating, setRating] = useState("5");
  const [content, setContent] = useState("不错");
  const [reviewItemId, setReviewItemId] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);

  async function reload() {
    if (!getToken()) return;
    const o = await apiFetch<Order>(`/api/v1/buyer/orders/${orderId}`);
    setOrder(o);
    if (o.items?.[0] && !reviewItemId) setReviewItemId(String(o.items[0].id));
    setShipments(await apiFetch<Shipment[]>(`/api/v1/buyer/orders/${orderId}/shipments`));
    setAftersales(await apiFetch<Aftersale[]>("/api/v1/buyer/aftersales").then((all) => all.filter((a) => a.orderId === orderId)));
  }

  useEffect(() => {
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, [orderId]);

  async function confirmReceipt() {
    setError(null);
    try {
      await apiFetch(`/api/v1/buyer/orders/${orderId}/confirm-receipt`, { method: "POST" });
      setMsg("已确认收货，可评价");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "确认失败");
    }
  }

  async function submitReview(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await apiFetch("/api/v1/buyer/reviews", {
        method: "POST",
        json: {
          orderId,
          orderItemId: Number(reviewItemId),
          rating: Number(rating),
          content
        }
      });
      setMsg("评价已提交");
    } catch (err) {
      setError(err instanceof Error ? err.message : "评价失败");
    }
  }

  async function applyAftersale(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await apiFetch("/api/v1/buyer/aftersales", {
        method: "POST",
        json: {
          orderId,
          type,
          reason,
          refundCents: Math.round(parseFloat(refundYuan) * 100)
        }
      });
      setMsg("售后已申请");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "申请失败");
    }
  }

  async function fillReverse(aftersaleId: number) {
    setError(null);
    try {
      await apiFetch(`/api/v1/buyer/aftersales/${aftersaleId}/reverse-tracking`, {
        method: "POST",
        json: { carrierCode: carrier, trackingNo: tracking }
      });
      setMsg("已填写退货运单");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "填写失败");
    }
  }

  return (
    <PageShell title={`订单 #${orderId}`} subtitle="确认收货 · 评价 · 物流 · 售后">
      <p><Link to="/">返回首页</Link></p>
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      {msg ? <p style={{ color: "green" }}>{msg}</p> : null}

      {order ? (
        <section>
          <h2>订单状态</h2>
          <p>{order.orderNo} · {order.status}</p>
          {(order.status === "PAID" || order.status === "FULFILLING") ? (
            <button type="button" onClick={confirmReceipt}>确认收货</button>
          ) : null}
          {order.status === "COMPLETED" ? (
            <form onSubmit={submitReview} style={{ marginTop: 8 }}>
              <h3>评价</h3>
              <select value={reviewItemId} onChange={(e) => setReviewItemId(e.target.value)}>
                {(order.items || []).map((it) => (
                  <option key={it.id} value={it.id}>{it.productTitle} (行#{it.id})</option>
                ))}
              </select>{" "}
              <select value={rating} onChange={(e) => setRating(e.target.value)}>
                {[5, 4, 3, 2, 1].map((n) => <option key={n} value={n}>{n}星</option>)}
              </select>{" "}
              <input value={content} onChange={(e) => setContent(e.target.value)} placeholder="评价内容" />{" "}
              <button type="submit">提交评价</button>
            </form>
          ) : null}
        </section>
      ) : null}

      <section>
        <h2>物流</h2>
        {shipments.length === 0 ? <p>暂无运单</p> : null}
        {shipments.map((s) => (
          <div key={s.id} style={{ marginBottom: 12 }}>
            <b>{s.direction}</b> {s.carrierCode}/{s.trackingNo} · {s.status}
            <ol>
              {(s.tracks || []).map((t, i) => (
                <li key={i}>{t.trackedAt}: [{t.status}] {t.description}</li>
              ))}
            </ol>
          </div>
        ))}
      </section>

      <section>
        <h2>申请售后</h2>
        <form onSubmit={applyAftersale}>
          <select value={type} onChange={(e) => setType(e.target.value as "REFUND_ONLY" | "RETURN_REFUND")}>
            <option value="REFUND_ONLY">仅退款</option>
            <option value="RETURN_REFUND">退货退款</option>
          </select>{" "}
          <input value={reason} onChange={(e) => setReason(e.target.value)} placeholder="原因" />{" "}
          <input value={refundYuan} onChange={(e) => setRefundYuan(e.target.value)} placeholder="退款元" />{" "}
          <button type="submit">提交</button>
        </form>
      </section>

      <section>
        <h2>我的售后</h2>
        <ul>
          {aftersales.map((a) => (
            <li key={a.id} style={{ marginBottom: 10 }}>
              {a.aftersaleNo} · {a.type} · {a.status} · ¥{(a.refundCents / 100).toFixed(2)}
              {a.type === "RETURN_REFUND" && a.status === "APPROVED" && !a.reverseShipmentId && (
                <div>
                  <input value={carrier} onChange={(e) => setCarrier(e.target.value)} placeholder="承运商" />{" "}
                  <input value={tracking} onChange={(e) => setTracking(e.target.value)} placeholder="退货运单号" />{" "}
                  <button type="button" onClick={() => fillReverse(a.id)}>填写逆向运单</button>
                </div>
              )}
            </li>
          ))}
        </ul>
      </section>
    </PageShell>
  );
}
