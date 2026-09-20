import { PageShell } from "@meiyue/ui";
import type { PingPayload } from "@meiyue/types";
import { useEffect, useState } from "react";

/**
 * 买家首页（最小可运行壳）
 * - 展示品牌与脚手架说明
 * - 可选探测后端 /api/v1/ping（后端未启动时显示提示，不阻塞页面）
 */
export function HomePage() {
  const [ping, setPing] = useState<PingPayload | null>(null);
  const [pingError, setPingError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    // 探测 API；失败仅提示，不影响壳页面渲染
    fetch("/api/v1/ping")
      .then(async (res) => {
        if (!res.ok) {
          throw new Error(`HTTP ${res.status}`);
        }
        const body = (await res.json()) as { data: PingPayload };
        if (!cancelled) {
          setPing(body.data);
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          setPingError(err instanceof Error ? err.message : "无法连接 API");
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <PageShell
      title="买家商城"
      subtitle="浏览、下单、物流跟踪与售后申请的入口壳。本轮仅脚手架。"
    >
      <ul>
        <li>工程标识：meiyue-mall / @meiyue/web-buyer</li>
        <li>下一迭代：I1 登录；I2 商品浏览与店铺装修只读</li>
        <li>硬约束：不做直播带货</li>
      </ul>
      <section>
        <h2>API 探测</h2>
        {ping ? (
          <pre>{JSON.stringify(ping, null, 2)}</pre>
        ) : (
          <p>{pingError ? `后端未就绪：${pingError}` : "探测中…"}</p>
        )}
      </section>
    </PageShell>
  );
}
