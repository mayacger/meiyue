import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import "./OrderDetailPage.css";

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

/**
 * 订单详情：确认收货 / 评价 / 物流 / 售后
 */
export function OrderDetailPage() {
  const { id } = useParams();
  const orderId = Number(id);
  const navigate = useNavigate();
  const [order, setOrder] = useState<Order | null>(null);
  const [shipments, setShipments] = useState<Shipment[]>([]);
  const [aftersales, setAftersales] = useState<Aftersale[]>([]);
  const [type, setType] = useState<"REFUND_ONLY" | "RETURN_REFUND">("REFUND_ONLY");
  const [reason, setReason] = useState("不想要了");
  const [refundYuan, setRefundYuan] = useState("99");
  const [rating, setRating] = useState("5");
  const [content, setContent] = useState("不错");
  const [reviewItemId, setReviewItemId] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);

  async function reload() {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    const o = await apiFetch<Order>(`/api/v1/buyer/orders/${orderId}`);
    setOrder(o);
    if (o.items?.[0] && !reviewItemId) setReviewItemId(String(o.items[0].id));
    setShipments(await apiFetch<Shipment[]>(`/api/v1/buyer/orders/${orderId}/shipments`));
    const all = await apiFetch<Aftersale[]>("/api/v1/buyer/aftersales");
    setAftersales(all.filter((a) => a.orderId === orderId));
  }

  useEffect(() => {
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, [orderId]);

  async function confirmReceipt() {
    try {
      await apiFetch(`/api/v1/buyer/orders/${orderId}/confirm-receipt`, { method: "POST" });
      setMsg("已确认收货");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "操作失败");
    }
  }

  async function submitReview(e: FormEvent) {
    e.preventDefault();
    try {
      await apiFetch("/api/v1/buyer/reviews", {
        method: "POST",
        json: {
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

  return (
    <div className="my-od">
      <p>
        <Link to="/orders">← 订单列表</Link>
      </p>
      <h1>{order?.orderNo ?? `订单 #${id}`}</h1>
      <p>状态：{order?.status}</p>
      {error ? <p className="my-error">{error}</p> : null}
      {msg ? <p className="my-ok">{msg}</p> : null}

      <section>
        <h2>商品</h2>
        <ul>
          {order?.items?.map((it) => (
            <li key={it.id}>
              {it.productTitle}{" "}
              <Link to={`/products/${it.productId}`}>查看</Link>
            </li>
          ))}
        </ul>
        {order?.status === "SHIPPED" || order?.status === "DELIVERED" ? (
          <button type="button" className="my-btn my-btn--primary" onClick={confirmReceipt}>
            确认收货
          </button>
        ) : null}
      </section>

      <section>
        <h2>物流</h2>
        {shipments.length === 0 ? <p className="my-muted">暂无物流</p> : null}
        {shipments.map((s) => (
          <div key={s.id} className="my-od__ship">
            <p>
              {s.direction} · {s.carrierCode} {s.trackingNo} · {s.status}
            </p>
            <ul>
              {s.tracks?.map((t, i) => (
                <li key={i}>
                  {t.status} · {t.description} · {t.trackedAt}
                </li>
              ))}
            </ul>
          </div>
        ))}
      </section>

      <section>
        <h2>评价</h2>
        <form onSubmit={submitReview} className="my-od__form">
          <select value={reviewItemId} onChange={(e) => setReviewItemId(e.target.value)}>
            {order?.items?.map((it) => (
              <option key={it.id} value={it.id}>
                {it.productTitle}
              </option>
            ))}
          </select>
          <input value={rating} onChange={(e) => setRating(e.target.value)} placeholder="评分 1-5" />
          <input value={content} onChange={(e) => setContent(e.target.value)} placeholder="评价内容" />
          <button type="submit" className="my-btn my-btn--ghost">
            提交评价
          </button>
        </form>
      </section>

      <section>
        <h2>售后</h2>
        <form onSubmit={applyAftersale} className="my-od__form">
          <select value={type} onChange={(e) => setType(e.target.value as typeof type)}>
            <option value="REFUND_ONLY">仅退款</option>
            <option value="RETURN_REFUND">退货退款</option>
          </select>
          <input value={reason} onChange={(e) => setReason(e.target.value)} />
          <input value={refundYuan} onChange={(e) => setRefundYuan(e.target.value)} placeholder="退款金额（元）" />
          <button type="submit" className="my-btn my-btn--ghost">
            申请售后
          </button>
        </form>
        <ul>
          {aftersales.map((a) => (
            <li key={a.id}>
              {a.aftersaleNo} · {a.type} · {a.status} · ¥{(a.refundCents / 100).toFixed(2)}
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
