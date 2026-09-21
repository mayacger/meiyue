import { View, Text, Button } from "@tarojs/components";
import Taro, { useDidShow } from "@tarojs/taro";
import { useState } from "react";
import type { NotificationItem } from "@meiyue/types";
import { apiFetch, getToken } from "../../services/api";
import "./index.css";

/**
 * 站内通知（I19）
 * 入口：我的 → 通知 · pages/notifications/index
 * API：GET /notifications · unread-count · POST read / read-all
 */
export default function NotificationsPage() {
  const [list, setList] = useState<NotificationItem[]>([]);
  const [unread, setUnread] = useState(0);
  const [error, setError] = useState("");

  async function reload() {
    if (!getToken()) {
      Taro.showToast({ title: "请先登录", icon: "none" });
      return;
    }
    const [items, count] = await Promise.all([
      apiFetch<NotificationItem[]>("/api/v1/notifications"),
      apiFetch<{ unread: number }>("/api/v1/notifications/unread-count")
    ]);
    setList(items);
    setUnread(count.unread ?? 0);
  }

  useDidShow(() => {
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  });

  async function markOne(id: number) {
    try {
      await apiFetch(`/api/v1/notifications/${id}/read`, { method: "POST" });
      await reload();
    } catch (e) {
      setError(e instanceof Error ? e.message : "标记失败");
    }
  }

  async function markAll() {
    try {
      await apiFetch("/api/v1/notifications/read-all", { method: "POST" });
      await reload();
    } catch (e) {
      setError(e instanceof Error ? e.message : "操作失败");
    }
  }

  return (
    <View className="page">
      <Text className="h1">站内通知</Text>
      <Text className="muted">未读 {unread} 条</Text>
      {error ? <Text className="err">{error}</Text> : null}
      {unread > 0 ? (
        <Button className="btn" size="mini" onClick={markAll}>
          全部已读
        </Button>
      ) : null}
      {list.length === 0 ? <Text className="muted">暂无通知</Text> : null}
      {list.map((n) => (
        <View key={n.id} className={`card ${n.read ? "read" : ""}`}>
          <Text className="title">{n.title}</Text>
          <Text className="meta">
            {n.category}
            {n.createdAt ? ` · ${n.createdAt}` : ""}
          </Text>
          <Text className="body">{n.body}</Text>
          {!n.read ? (
            <Button size="mini" onClick={() => markOne(n.id)}>
              标已读
            </Button>
          ) : (
            <Text className="muted">已读</Text>
          )}
        </View>
      ))}
    </View>
  );
}
