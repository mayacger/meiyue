import { FormEvent, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiFetch } from "@meiyue/api";
import { PageShell } from "@meiyue/ui";

interface OrderItem {
  id: number;
  productTitle: string;
  skuCode: string;
  quantity: number;
  lineTotalCents: number;
}

interface Order {
  id: number;
  orderNo: string;
  status: string;
  totalCents: number;
  items: OrderItem[];
}

interface Shipment {
  id: number;
  orderId: number;
  status: string;
  carrierCode: string;
  trackingNo: string;
  tracks: { status: string; description: string }[];
}

const FORWARD_FLOW = ["PENDING_PICKUP", "PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED"];

/** 商家发货与运单状态推进 */
export function ShipmentsPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [shipments, setShipments] = useState<Shipment[]>([]);
  const [orderId, setOrderId] = useState("");
  const [carrier, setCarrier] = useState("SF");
  const [tracking, setTracking] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);

  async function reload() {
    setOrders(await apiFetch<Order[]>("/api/v1/seller/orders"));
    setShipments(await apiFetch<Shipment[]>("/api/v1/seller/shipments"));
  }

  useEffect(() => {
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, []);

  async function createShip(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setMsg(null);
    try {
      await apiFetch("/api/v1/seller/shipments", {
        method: "POST",
        json: {
          orderId: Number(orderId),
          carrierCode: carrier,
          trackingNo: tracking,
          receiverName: "买家",
          receiverPhone: "",
          receiverAddress: ""
        }
      });
      setTracking("");
      setMsg("已创建运单");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "发货失败");
    }
  }

  async function advance(id: number, status: string) {
    setError(null);
    try {
      await apiFetch(`/api/v1/seller/shipments/${id}/status`, {
        method: "POST",
        json: { status, description: `推进到 ${status}` }
      });
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "状态更新失败");
    }
  }

  async function syncTracks(id: number) {
    try {
      await apiFetch(`/api/v1/seller/shipments/${id}/sync-tracks`, { method: "POST" });
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "同步轨迹失败");
    }
  }

  function nextStatus(cur: string): string | null {
    const i = FORWARD_FLOW.indexOf(cur);
    if (i < 0 || i >= FORWARD_FLOW.length - 1) return null;
    return FORWARD_FLOW[i + 1];
  }

  return (
    <PageShell title="发货管理" subtitle="创建运单并推进正向物流状态">
      <p><Link to="/">返回概览</Link></p>
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      {msg ? <p style={{ color: "green" }}>{msg}</p> : null}

      <section>
        <h2>可履约订单</h2>
        <ul>
          {orders.map((o) => (
            <li key={o.id}>
              #{o.id} {o.orderNo} · {o.status} · ¥{(o.totalCents / 100).toFixed(2)}
              <button type="button" style={{ marginLeft: 8 }} onClick={() => setOrderId(String(o.id))}>选中发货</button>
            </li>
          ))}
        </ul>
      </section>

      <section>
        <h2>新建运单</h2>
        <form onSubmit={createShip}>
          <label>订单ID <input value={orderId} onChange={(e) => setOrderId(e.target.value)} required /></label>{" "}
          <label>承运商 <input value={carrier} onChange={(e) => setCarrier(e.target.value)} /></label>{" "}
          <label>运单号 <input value={tracking} onChange={(e) => setTracking(e.target.value)} required /></label>{" "}
          <button type="submit">发货</button>
        </form>
      </section>

      <section>
        <h2>我的运单</h2>
        <ul>
          {shipments.map((s) => {
            const next = nextStatus(s.status);
            return (
              <li key={s.id} style={{ marginBottom: 12 }}>
                运单#{s.id} 订单{s.orderId} · {s.carrierCode}/{s.trackingNo} · <b>{s.status}</b>
                {" "}
                {next ? <button type="button" onClick={() => advance(s.id, next)}>→ {next}</button> : null}
                {" "}
                <button type="button" onClick={() => syncTracks(s.id)}>同步轨迹</button>
                <ul>
                  {(s.tracks || []).map((t, idx) => (
                    <li key={idx}>{t.status}: {t.description}</li>
                  ))}
                </ul>
              </li>
            );
          })}
        </ul>
      </section>
    </PageShell>
  );
}
