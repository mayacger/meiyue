import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { PageShell } from "@meiyue/ui";

/** 商品详情（公开） */
export function ProductDetailPage() {
  const { id } = useParams();
  const [data, setData] = useState<Record<string, unknown> | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch(`/api/v1/products/${id}`)
      .then(async (res) => {
        const body = await res.json();
        if (!body.success) throw new Error(body.message);
        setData(body.data);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"));
  }, [id]);

  return (
    <PageShell title="商品详情" subtitle={`商品 #${id}`}>
      <p><Link to="/">返回首页</Link></p>
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      {data ? <pre>{JSON.stringify(data, null, 2)}</pre> : null}
    </PageShell>
  );
}
