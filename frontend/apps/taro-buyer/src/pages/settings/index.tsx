import { View, Text, Button, Input } from "@tarojs/components";
import Taro, { useDidShow } from "@tarojs/taro";
import { useState } from "react";
import type { UserProfile } from "@meiyue/types";
import { apiFetch, getToken } from "../../services/api";
import "./index.css";

/**
 * 个人设置（I24 Taro）
 * API：GET /auth/me · PUT /auth/profile · POST /auth/password
 */
export default function SettingsPage() {
  const [me, setMe] = useState<UserProfile | null>(null);
  const [displayName, setDisplayName] = useState("");
  const [phone, setPhone] = useState("");
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [error, setError] = useState("");

  async function load() {
    if (!getToken()) {
      Taro.showToast({ title: "请先登录", icon: "none" });
      Taro.switchTab({ url: "/pages/mine/index" });
      return;
    }
    const u = await apiFetch<UserProfile>("/api/v1/auth/me");
    setMe(u);
    setDisplayName(u.displayName || "");
    setPhone(u.phone || "");
  }

  useDidShow(() => {
    load().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  });

  async function saveProfile() {
    setError("");
    try {
      const u = await apiFetch<UserProfile>("/api/v1/auth/profile", {
        method: "PUT",
        data: { displayName, phone }
      });
      setMe(u);
      Taro.showToast({ title: "资料已保存", icon: "success" });
    } catch (e) {
      setError(e instanceof Error ? e.message : "保存失败");
    }
  }

  async function changePassword() {
    setError("");
    // I35：改密二次确认
    const { confirm } = await Taro.showModal({
      title: "确认修改登录密码？",
      content: "修改后请使用新密码登录。",
      confirmText: "确认修改",
      cancelText: "取消"
    });
    if (!confirm) {
      return;
    }
    try {
      await apiFetch("/api/v1/auth/password", {
        method: "POST",
        data: { oldPassword, newPassword }
      });
      setOldPassword("");
      setNewPassword("");
      Taro.showToast({ title: "密码已更新", icon: "success" });
    } catch (e) {
      setError(e instanceof Error ? e.message : "改密失败");
    }
  }

  return (
    <View className="page">
      <Text className="brand">美月商城</Text>
      <Text className="h1">个人设置</Text>
      <Text className="muted">账号 {me?.username || "…"}</Text>
      {error ? <Text className="err">{error}</Text> : null}

      <Text className="h2">资料</Text>
      <Input
        className="input"
        value={displayName}
        onInput={(e) => setDisplayName(e.detail.value)}
        placeholder="展示名"
      />
      <Input
        className="input"
        value={phone}
        onInput={(e) => setPhone(e.detail.value)}
        placeholder="手机"
      />
      <Button className="btn" onClick={saveProfile}>
        保存资料
      </Button>

      <Text className="h2">修改密码</Text>
      <Input
        className="input"
        password
        value={oldPassword}
        onInput={(e) => setOldPassword(e.detail.value)}
        placeholder="当前密码"
      />
      <Input
        className="input"
        password
        value={newPassword}
        onInput={(e) => setNewPassword(e.detail.value)}
        placeholder="新密码（至少6位）"
      />
      <Button className="btn ghost" onClick={changePassword}>
        更新密码
      </Button>
    </View>
  );
}
