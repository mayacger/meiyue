import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import "./TicketsPage.css";

interface Ticket {
  id: number;
  ticketNo: string;
  subject: string;
  body: string;
  category: string;
  status: string;
  sellerReply: string | null;
  adminReply: string | null;
  createdAt: string;
}

/**
 * 买家客服工单（I21 MVP）
 * 入口：/tickets · POST/GET /buyer/tickets · close
 */
export function TicketsPage() {
  const navigate = useNavigate();
  const [list, setList] = useState<Ticket[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);
  const [subject, setSubject] = useState("");
  const [body, setBody] = useState("");
  const [category, setCategory] = useState("GENERAL");

  async function reload() {
    setList(await apiFetch<Ticket[]>("/api/v1/buyer/tickets"));
  }

  useEffect(() => {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, [navigate]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setMsg(null);
    try {
      await apiFetch("/api/v1/buyer/tickets", {
        method: "POST",
        json: { subject, body, category, tenantId: null }
      });
      setSubject("");
      setBody("");
      setMsg("工单已提交");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "提交失败");
    }
  }

  async function close(id: number) {
    try {
      await apiFetch(`/api/v1/buyer/tickets/${id}/close`, { method: "POST" });
      setMsg("已关闭");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "关闭失败");
    }
  }

  return (
    <div className="my-ticket my-page">
      <h1 className="my-page-title my-fade-up">客服工单</h1>
      <p className="my-page-lead">
        留言式咨询（非即时聊天）· <Link to="/orders">我的订单</Link>
      </p>
      {error ? <p className="my-error">{error}</p> : null}
      {msg ? <p className="my-ok">{msg}</p> : null}

      <form className="my-ticket__form my-fade-up" onSubmit={onSubmit}>
        <h2>新建工单</h2>
        <label>
          分类
          <select value={category} onChange={(e) => setCategory(e.target.value)}>
            <option value="GENERAL">一般咨询</option>
            <option value="ORDER">订单</option>
            <option value="AFTERSALE">售后</option>
            <option value="PRODUCT">商品</option>
          </select>
        </label>
        <label>
          标题
          <input required value={subject} onChange={(e) => setSubject(e.target.value)} />
        </label>
        <label>
          描述
          <textarea required rows={4} value={body} onChange={(e) => setBody(e.target.value)} />
        </label>
        <button type="submit" className="my-btn my-btn--primary">
          提交工单
        </button>
      </form>

      <div className="my-ticket__list my-fade-up">
        <h2>我的工单（{list.length}）</h2>
        {list.length === 0 ? <div className="my-empty">暂无工单</div> : null}
        <ul>
          {list.map((t) => (
            <li key={t.id}>
              <div>
                <strong>
                  {t.ticketNo} · {t.subject}
                </strong>
                <span className="my-muted">
                  {" "}
                  · {t.category} · {t.status}
                </span>
                <p>{t.body}</p>
                {t.sellerReply ? <p className="my-muted">商家：{t.sellerReply}</p> : null}
                {t.adminReply ? <p className="my-muted">平台：{t.adminReply}</p> : null}
              </div>
              {t.status !== "CLOSED" ? (
                <button type="button" className="my-btn" onClick={() => close(t.id)}>
                  关闭
                </button>
              ) : null}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
