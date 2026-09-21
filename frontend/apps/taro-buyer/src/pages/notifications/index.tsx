import { View, Text, Button } from "@tarojs/components";
import Taro, { useDidShow } from "@tarojs/taro";
import { useState } from "react";
import type { NotificationItem } from "@meiyue/types";
import { apiFetch, getToken } from "../../services/api";
import "./index.css";

/**
 * 消息中心（I19 + I25 已读未读筛选）
 * 入口：我的 → 通知
 */
export default function NotificationsPage() {
  const [list, setList] = useState<NotificationItem[]>([]);
  const [unread, setUnread] = useState(0);
  const [filter, setFilter] = useState<"all" | "unread">("all");
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

  const shown = filter === "unread" ? list.filter((n) => !n.read) : list;

  return (
    <View className="page">
      <Text className="h1">消息中心</Text>
      <Text className="muted">未读 {unread} 条</Text>
      <View className="filters">
        <Button
          size="mini"
          className={filter === "all" ? "btn" : "btn ghost"}
          onClick={() => setFilter("all")}
        >
          全部
        </Button>
        <Button
          size="mini"
          className={filter === "unread" ? "btn" : "btn ghost"}
          onClick={() => setFilter("unread")}
        >
          未读
        </Button>
      </View>
      {error ? <Text className="err">{error}</Text> : null}
      {unread > 0 ? (
        <Button className="btn" size="mini" onClick={markAll}>
          全部已读
        </Button>
      ) : null}
      {shown.length === 0 ? (
        <Text className="muted empty">
          {filter === "unread" ? "没有未读消息" : "暂无通知"}
        </Text>
      ) : null}
      {shown.map((n) => (
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
