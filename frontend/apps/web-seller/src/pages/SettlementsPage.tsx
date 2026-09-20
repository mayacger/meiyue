import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiFetch } from "@meiyue/api";
import { PageShell } from "@meiyue/ui";

interface Bill {
  id: number;
  orderId: number;
  entryType: string;
  amountCents: number;
  status: string;
  periodKey: string;
  remark: string | null;
  createdAt: string;
}

interface Period {
  periodKey: string;
  status: string;
  amountCents: number;
  entryCount: number;
}

/** 商家结算账单（MVP 记账） */
export function SettlementsPage() {
  const [bills, setBills] = useState<Bill[]>([]);
  const [periods, setPeriods] = useState<Period[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        setPeriods(await apiFetch<Period[]>("/api/v1/seller/settlements/periods"));
        setBills(await apiFetch<Bill[]>("/api/v1/seller/settlements/bills"));
      } catch (e) {
        setError(e instanceof Error ? e.message : "加载失败");
      }
    })();
  }, []);

  return (
    <PageShell title="结算账本" subtitle="周期汇总 + 明细（不自动打款）">
      <p><Link to="/">返回概览</Link></p>
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      <section>
        <h2>周期汇总</h2>
        <ul>
          {periods.map((p, i) => (
            <li key={`${p.periodKey}-${p.status}-${i}`}>
              {p.periodKey} · {p.status} · ¥{(p.amountCents / 100).toFixed(2)} · {p.entryCount} 笔
            </li>
          ))}
        </ul>
      </section>
      <section>
        <h2>账单明细</h2>
        <ul>
          {bills.map((b) => (
            <li key={b.id}>
              #{b.id} 订单{b.orderId} · {b.entryType} · ¥{(b.amountCents / 100).toFixed(2)} · {b.status} · {b.periodKey}
            </li>
          ))}
        </ul>
      </section>
    </PageShell>
  );
}
