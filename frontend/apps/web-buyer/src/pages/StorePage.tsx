import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { PageShell } from "@meiyue/ui";

/** 店铺已发布装修页 + 本店商品 */
export function StorePage() {
  const { tenantId } = useParams();
  const [page, setPage] = useState<Record<string, unknown> | null>(null);
  const [products, setProducts] = useState<unknown[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      fetch(`/api/v1/stores/${tenantId}/page`).then((r) => r.json()),
      fetch(`/api/v1/stores/${tenantId}/products`).then((r) => r.json())
    ])
      .then(([pageBody, prodBody]) => {
        if (pageBody.success) setPage(pageBody.data);
        else setError(pageBody.message);
        if (prodBody.success) setProducts(prodBody.data);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"));
  }, [tenantId]);

  const theme = (page?.themeColor as string) || "#1a5f4a";

  return (
    <PageShell title="店铺主页" subtitle={`租户 #${tenantId} 已发布装修`}>
      <p><Link to="/">返回首页</Link></p>
      <div style={{ height: 8, background: theme, marginBottom: 12 }} />
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      {page ? (
        <section>
          <h2>装修楼层</h2>
          <pre>{String(page.floorsJson)}</pre>
        </section>
      ) : null}
      <section>
        <h2>本店商品</h2>
        <pre>{JSON.stringify(products, null, 2)}</pre>
      </section>
    </PageShell>
  );
}
