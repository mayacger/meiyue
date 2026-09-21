import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import type { NotificationItem } from "@meiyue/types";
import "./NotificationsPage.css";

/**
 * 买家站内通知（I19）
 * 入口：顶栏「通知」· /notifications
 * API：GET /notifications · unread-count · POST /{id}/read · read-all
 * 字段：id / title / body / category / read / createdAt
 */
export function NotificationsPage() {
  const navigate = useNavigate();
  const [list, setList] = useState<NotificationItem[]>([]);
  const [unread, setUnread] = useState(0);
  const [error, setError] = useState<string | null>(null);

  async function reload() {
    const [items, count] = await Promise.all([
      apiFetch<NotificationItem[]>("/api/v1/notifications"),
      apiFetch<{ unread: number }>("/api/v1/notifications/unread-count")
    ]);
    setList(items);
    setUnread(count.unread ?? 0);
  }

  useEffect(() => {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, [navigate]);

  async function markOne(id: number) {
    setError(null);
    try {
      await apiFetch(`/api/v1/notifications/${id}/read`, { method: "POST" });
      await reload();
    } catch (e) {
      setError(e instanceof Error ? e.message : "标记失败");
    }
  }

  async function markAll() {
    setError(null);
    try {
      await apiFetch("/api/v1/notifications/read-all", { method: "POST" });
      await reload();
    } catch (e) {
      setError(e instanceof Error ? e.message : "操作失败");
    }
  }

  return (
    <div className="my-notify my-page">
      <div className="my-notify__head my-fade-up">
        <div>
          <h1 className="my-page-title">站内通知</h1>
          <p className="my-page-lead">
            未读 {unread} 条 · <Link to="/orders">我的订单</Link>
          </p>
        </div>
        {unread > 0 ? (
          <button type="button" className="my-btn" onClick={markAll}>
            全部已读
          </button>
        ) : null}
      </div>
      {error ? <p className="my-error">{error}</p> : null}
      {list.length === 0 ? (
        <div className="my-empty">暂无通知</div>
      ) : (
        <ul className="my-notify__list my-fade-up">
          {list.map((n) => (
            <li key={n.id} className={n.read ? "is-read" : "is-unread"}>
              <div>
                <strong>{n.title}</strong>
                <span className="my-notify__meta">
                  {n.category}
                  {n.createdAt ? ` · ${n.createdAt}` : ""}
                </span>
                <p>{n.body}</p>
              </div>
              {!n.read ? (
                <button type="button" className="my-btn" onClick={() => markOne(n.id)}>
                  标已读
                </button>
              ) : (
                <span className="my-muted">已读</span>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
