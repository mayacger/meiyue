import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import type { BuyerAddress } from "@meiyue/types";
import "./AddressPage.css";

/**
 * 买家地址簿（I18）
 * 入口：顶栏「地址」· /addresses
 * API：
 * - GET/POST /buyer/addresses
 * - PUT/DELETE /buyer/addresses/{id}
 * - POST /buyer/addresses/{id}/default
 *
 * 字段：receiverName / receiverPhone / province / city / district /
 * detailAddress / defaultAddress
 */
export function AddressPage() {
  const navigate = useNavigate();
  const [list, setList] = useState<BuyerAddress[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState({
    receiverName: "",
    receiverPhone: "",
    province: "上海市",
    city: "上海市",
    district: "浦东新区",
    detailAddress: "",
    defaultAddress: false
  });

  async function reload() {
    const data = await apiFetch<BuyerAddress[]>("/api/v1/buyer/addresses");
    setList(data);
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
    setForm({
      receiverName: "",
      receiverPhone: "",
      province: "上海市",
      city: "上海市",
      district: "浦东新区",
      detailAddress: "",
      defaultAddress: false
    });
  }

  function startEdit(a: BuyerAddress) {
    setEditingId(a.id);
    setForm({
      receiverName: a.receiverName,
      receiverPhone: a.receiverPhone,
      province: a.province,
      city: a.city,
      district: a.district,
      detailAddress: a.detailAddress,
      defaultAddress: a.defaultAddress
    });
    setMsg(null);
    setError(null);
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setMsg(null);
    try {
      if (editingId) {
        await apiFetch(`/api/v1/buyer/addresses/${editingId}`, {
          method: "PUT",
          json: form
        });
        setMsg("地址已更新");
      } else {
        await apiFetch("/api/v1/buyer/addresses", {
          method: "POST",
          json: form
        });
        setMsg("地址已新增");
      }
      resetForm();
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "保存失败");
    }
  }

  async function setDefault(id: number) {
    setError(null);
    try {
      await apiFetch(`/api/v1/buyer/addresses/${id}/default`, { method: "POST" });
      setMsg("已设为默认地址");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "设置失败");
    }
  }

  async function remove(id: number) {
    setError(null);
    try {
      await apiFetch(`/api/v1/buyer/addresses/${id}`, { method: "DELETE" });
      setMsg("已删除");
      if (editingId === id) resetForm();
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "删除失败");
    }
  }

  return (
    <div className="my-addr my-page">
      <h1 className="my-page-title my-fade-up">收货地址</h1>
      <p className="my-page-lead">
        对接真实地址簿 API · <Link to="/checkout">去结算</Link> · <Link to="/orders">我的订单</Link>
      </p>
      {error ? <p className="my-error">{error}</p> : null}
      {msg ? <p className="my-ok">{msg}</p> : null}

      <div className="my-addr__grid my-fade-up">
        <form className="my-addr__form" onSubmit={onSubmit}>
          <h2>{editingId ? `编辑 #${editingId}` : "新增地址"}</h2>
          <label>
            收件人
            <input
              required
              value={form.receiverName}
              onChange={(e) => setForm({ ...form, receiverName: e.target.value })}
            />
          </label>
          <label>
            手机
            <input
              required
              value={form.receiverPhone}
              onChange={(e) => setForm({ ...form, receiverPhone: e.target.value })}
            />
          </label>
          <div className="my-addr__row">
            <label>
              省
              <input
                required
                value={form.province}
                onChange={(e) => setForm({ ...form, province: e.target.value })}
              />
            </label>
            <label>
              市
              <input
                required
                value={form.city}
                onChange={(e) => setForm({ ...form, city: e.target.value })}
              />
            </label>
            <label>
              区
              <input
                required
                value={form.district}
                onChange={(e) => setForm({ ...form, district: e.target.value })}
              />
            </label>
          </div>
          <label>
            详细地址
            <input
              required
              value={form.detailAddress}
              onChange={(e) => setForm({ ...form, detailAddress: e.target.value })}
            />
          </label>
          <label className="my-addr__check">
            <input
              type="checkbox"
              checked={form.defaultAddress}
              onChange={(e) => setForm({ ...form, defaultAddress: e.target.checked })}
            />
            设为默认
          </label>
          <div className="my-addr__actions">
            <button type="submit" className="my-btn my-btn--primary">
              {editingId ? "保存修改" : "新增地址"}
            </button>
            {editingId ? (
              <button type="button" className="my-btn" onClick={resetForm}>
                取消编辑
              </button>
            ) : null}
          </div>
        </form>

        <div className="my-addr__list">
          <h2>已保存（{list.length}）</h2>
          {list.length === 0 ? (
            <div className="my-empty">暂无地址，请在左侧新增</div>
          ) : (
            <ul>
              {list.map((a) => (
                <li key={a.id}>
                  <div>
                    <strong>
                      {a.receiverName} · {a.receiverPhone}
                    </strong>
                    {a.defaultAddress ? <span className="my-addr__badge">默认</span> : null}
                    <p className="my-muted">
                      {a.province}
                      {a.city}
                      {a.district} {a.detailAddress}
                    </p>
                  </div>
                  <div className="my-addr__ops">
                    <button type="button" className="my-btn" onClick={() => startEdit(a)}>
                      编辑
                    </button>
                    {!a.defaultAddress ? (
                      <button type="button" className="my-btn" onClick={() => setDefault(a.id)}>
                        设默认
                      </button>
                    ) : null}
                    <button type="button" className="my-btn" onClick={() => remove(a.id)}>
                      删除
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
