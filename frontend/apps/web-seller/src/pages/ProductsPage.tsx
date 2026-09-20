import { FormEvent, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiFetch } from "@meiyue/api";
import { PageShell } from "@meiyue/ui";

interface Category {
  id: number;
  name: string;
}
interface Product {
  id: number;
  title: string;
  status: string;
  skus: { skuCode: string; priceCents: number; stockQty: number }[];
}

/** 商家商品管理（I2 最小页） */
export function ProductsPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [list, setList] = useState<Product[]>([]);
  const [title, setTitle] = useState("");
  const [categoryId, setCategoryId] = useState<number | "">("");
  const [skuCode, setSkuCode] = useState("");
  const [priceYuan, setPriceYuan] = useState("99");
  const [stock, setStock] = useState("10");
  const [error, setError] = useState<string | null>(null);

  async function reload() {
    const products = await apiFetch<Product[]>("/api/v1/seller/products");
    setList(products);
  }

  useEffect(() => {
    apiFetch<Category[]>("/api/v1/categories").then(setCategories).catch(() => undefined);
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, []);

  async function onCreate(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await apiFetch("/api/v1/seller/products", {
        method: "POST",
        json: {
          categoryId: categoryId === "" ? null : categoryId,
          title,
          subtitle: "",
          detailHtml: "",
          skus: [
            {
              skuCode,
              specText: "默认",
              priceCents: Math.round(parseFloat(priceYuan) * 100),
              stockQty: parseInt(stock, 10)
            }
          ]
        }
      });
      setTitle("");
      setSkuCode("");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "创建失败");
    }
  }

  async function setStatus(id: number, status: string) {
    await apiFetch(`/api/v1/seller/products/${id}/status`, {
      method: "POST",
      json: { status }
    });
    await reload();
  }

  return (
    <PageShell title="商品管理" subtitle="发布 SPU/SKU；可上架/下架（I2）">
      <p>
        <Link to="/">返回概览</Link> · <Link to="/decoration">店铺装修</Link>
      </p>
      <form onSubmit={onCreate} style={{ display: "grid", gap: 8, maxWidth: 420 }}>
        <label>
          类目
          <select value={categoryId} onChange={(e) => setCategoryId(e.target.value ? Number(e.target.value) : "")} required>
            <option value="">请选择</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          标题
          <input value={title} onChange={(e) => setTitle(e.target.value)} required />
        </label>
        <label>
          SKU 编码
          <input value={skuCode} onChange={(e) => setSkuCode(e.target.value)} required />
        </label>
        <label>
          价格（元）
          <input value={priceYuan} onChange={(e) => setPriceYuan(e.target.value)} required />
        </label>
        <label>
          库存
          <input value={stock} onChange={(e) => setStock(e.target.value)} required />
        </label>
        {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
        <button type="submit">创建草稿商品</button>
      </form>
      <h2>我的商品</h2>
      <ul>
        {list.map((p) => (
          <li key={p.id}>
            #{p.id} {p.title} [{p.status}] ¥
            {((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}
            {" "}
            <button type="button" onClick={() => setStatus(p.id, "ON_SALE")}>
              上架
            </button>{" "}
            <button type="button" onClick={() => setStatus(p.id, "OFF_SALE")}>
              下架
            </button>
          </li>
        ))}
      </ul>
    </PageShell>
  );
}
