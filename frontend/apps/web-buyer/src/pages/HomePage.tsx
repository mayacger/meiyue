import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { PageShell } from "@meiyue/ui";

interface Product {
  id: number;
  tenantId: number;
  title: string;
  subtitle: string | null;
  skus: { priceCents: number }[];
}

/**
 * 买家首页：浏览已上架商品（I2）
 */
export function HomePage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch("/api/v1/products")
      .then(async (res) => {
        const body = await res.json();
        if (!body.success) throw new Error(body.message);
        setProducts(body.data);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"));
  }, []);

  return (
    <PageShell title="买家商城" subtitle="浏览已上架商品。无直播带货。">
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      <ul>
        {products.map((p) => (
          <li key={p.id}>
            <Link to={`/products/${p.id}`}>
              {p.title}
            </Link>{" "}
            — ¥{((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}
            {" "}
            <Link to={`/stores/${p.tenantId}`}>进店</Link>
          </li>
        ))}
      </ul>
      {products.length === 0 && !error ? <p>暂无上架商品。</p> : null}
    </PageShell>
  );
}
