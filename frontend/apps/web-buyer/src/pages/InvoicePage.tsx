import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import type { InvoiceProfile } from "@meiyue/types";
import "./AddressPage.css";

/**
 * 发票抬头管理（I33 占位）
 *
 * 入口：顶栏 / 结算页链接 · /invoices
 * API：GET/POST/PUT/DELETE /buyer/invoice-profiles · POST .../default
 * 关系：结算页选择抬头 → 写入订单发票快照字段（无真实开票）
 */
export function InvoicePage() {
  const navigate = useNavigate();
  const [list, setList] = useState<InvoiceProfile[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState({
    title: "",
    taxNo: "",
    invoiceType: "PERSONAL",
    defaultProfile: false
  });

  async function reload() {
    setList(await apiFetch<InvoiceProfile[]>("/api/v1/buyer/invoice-profiles"));
  }

  useEffect(() => {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, [navigate]);

  function resetForm() {
    setEditingId(null);
    setForm({ title: "", taxNo: "", invoiceType: "PERSONAL", defaultProfile: false });
  }

  function startEdit(p: InvoiceProfile) {
    setEditingId(p.id);
    setForm({
      title: p.title,
      taxNo: p.taxNo || "",
      invoiceType: p.invoiceType || "PERSONAL",
      defaultProfile: p.defaultProfile
    });
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      if (editingId) {
        await apiFetch(`/api/v1/buyer/invoice-profiles/${editingId}`, {
          method: "PUT",
          json: form
        });
        setMsg("已更新抬头");
      } else {
        await apiFetch("/api/v1/buyer/invoice-profiles", { method: "POST", json: form });
        setMsg("已新增抬头");
      }
      resetForm();
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "保存失败");
    }
  }

  return (
    <div className="my-addr my-page">
      <h1 className="my-page-title my-fade-up">发票抬头</h1>
      <p className="my-page-lead">
        占位能力，无真实开票 · <Link to="/checkout">去结算选用</Link>
      </p>
      {error ? <p className="my-error">{error}</p> : null}
      {msg ? <p className="my-ok">{msg}</p> : null}
      <form onSubmit={onSubmit} className="my-addr__form my-fade-up">
        <label>
          抬头
          <input
            required
            maxLength={128}
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
          />
        </label>
        <label>
          税号
          <input
            maxLength={64}
            value={form.taxNo}
            onChange={(e) => setForm({ ...form, taxNo: e.target.value })}
            placeholder="企业抬头建议填写"
          />
        </label>
        <label>
          类型
          <select
            value={form.invoiceType}
            onChange={(e) => setForm({ ...form, invoiceType: e.target.value })}
          >
            <option value="PERSONAL">个人</option>
            <option value="COMPANY">企业</option>
          </select>
        </label>
        <label className="my-addr__check">
          <input
            type="checkbox"
            checked={form.defaultProfile}
            onChange={(e) => setForm({ ...form, defaultProfile: e.target.checked })}
          />
          设为默认
        </label>
        <div className="my-addr__actions">
          <button type="submit" className="my-btn my-btn--primary">
            {editingId ? "保存修改" : "新增抬头"}
          </button>
          {editingId ? (
            <button type="button" className="my-btn my-btn--ghost" onClick={resetForm}>
              取消编辑
            </button>
          ) : null}
        </div>
      </form>
      <ul className="my-addr__list my-fade-up">
        {list.map((p) => (
          <li key={p.id}>
            <div>
              <strong>
                [{p.invoiceType}] {p.title}
              </strong>
              {p.defaultProfile ? <span className="my-ok"> 默认</span> : null}
              <div className="my-muted">{p.taxNo || "无税号"}</div>
            </div>
            <div className="my-addr__ops">
              <button type="button" className="my-btn my-btn--ghost" onClick={() => startEdit(p)}>
                编辑
              </button>
              <button
                type="button"
                className="my-btn my-btn--ghost"
                onClick={() =>
                  apiFetch(`/api/v1/buyer/invoice-profiles/${p.id}/default`, { method: "POST" })
                    .then(reload)
                    .catch((e) => setError(e instanceof Error ? e.message : "设置失败"))
                }
              >
                默认
              </button>
              <button
                type="button"
                className="my-btn my-btn--ghost"
                onClick={() =>
                  apiFetch(`/api/v1/buyer/invoice-profiles/${p.id}`, { method: "DELETE" })
                    .then(reload)
                    .catch((e) => setError(e instanceof Error ? e.message : "删除失败"))
                }
              >
                删除
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
