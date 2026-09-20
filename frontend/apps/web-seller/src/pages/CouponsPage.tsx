import { FormEvent, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiFetch } from "@meiyue/api";
import { PageShell } from "@meiyue/ui";

interface Coupon {
  id: number;
  code: string;
  title: string;
  discountCents: number;
  minSpendCents: number;
  totalQuota: number;
  claimedCount: number;
  status: string;
}

/** 商家店券 */
export function CouponsPage() {
  const [list, setList] = useState<Coupon[]>([]);
  const [code, setCode] = useState("WELCOME10");
  const [title, setTitle] = useState("新人减10元");
  const [discountYuan, setDiscountYuan] = useState("10");
  const [minYuan, setMinYuan] = useState("50");
  const [quota, setQuota] = useState("100");
  const [error, setError] = useState<string | null>(null);

  async function reload() {
    setList(await apiFetch<Coupon[]>("/api/v1/seller/coupons"));
  }

  useEffect(() => {
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, []);

  async function onCreate(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await apiFetch("/api/v1/seller/coupons", {
        method: "POST",
        json: {
          code,
          title,
          discountCents: Math.round(parseFloat(discountYuan) * 100),
          minSpendCents: Math.round(parseFloat(minYuan) * 100),
          totalQuota: parseInt(quota, 10)
        }
      });
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "创建失败");
    }
  }

  return (
    <PageShell title="店券" subtitle="店铺发券；与平台券默认互斥（MUTUAL_EXCLUSIVE）">
      <p><Link to="/">返回概览</Link></p>
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      <form onSubmit={onCreate}>
        <input value={code} onChange={(e) => setCode(e.target.value)} placeholder="券码" required />{" "}
        <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="标题" required />{" "}
        <input value={discountYuan} onChange={(e) => setDiscountYuan(e.target.value)} placeholder="面额元" />{" "}
        <input value={minYuan} onChange={(e) => setMinYuan(e.target.value)} placeholder="门槛元" />{" "}
        <input value={quota} onChange={(e) => setQuota(e.target.value)} placeholder="总量" />{" "}
        <button type="submit">创建</button>
      </form>
      <ul>
        {list.map((c) => (
          <li key={c.id}>
            {c.code} · {c.title} · 减¥{(c.discountCents / 100).toFixed(2)} · 满¥{(c.minSpendCents / 100).toFixed(2)}
            · 已领 {c.claimedCount}/{c.totalQuota || "∞"} · {c.status}
          </li>
        ))}
      </ul>
    </PageShell>
  );
}
