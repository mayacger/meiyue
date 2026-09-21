import { View, Text, Input, Button } from "@tarojs/components";
import { useState } from "react";
import Taro, { useDidShow } from "@tarojs/taro";
import type { AuthResult, UserProfile } from "@meiyue/types";
import { apiFetch, getToken, setToken } from "../../services/api";
import "./index.css";

/**
 * 我的：登录 / 退出 / 展示资料
 * 演示：buyer1 / buyer123
 */
export default function MinePage() {
  const [username, setUsername] = useState("buyer1");
  const [password, setPassword] = useState("buyer123");
  const [me, setMe] = useState<UserProfile | null>(null);
  const [error, setError] = useState("");

  async function loadMe() {
    if (!getToken()) {
      setMe(null);
      return;
    }
    try {
      setMe(await apiFetch<UserProfile>("/api/v1/auth/me"));
    } catch {
      setMe(null);
    }
  }

  useDidShow(() => {
    loadMe();
  });

  async function login() {
    setError("");
    try {
      const data = await apiFetch<AuthResult>("/api/v1/auth/login", {
        method: "POST",
        data: { username, password }
      });
      setToken(data.accessToken);
      setMe(data.user);
      Taro.showToast({ title: "登录成功", icon: "success" });
    } catch (e) {
      setError(e instanceof Error ? e.message : "登录失败");
    }
  }

  function logout() {
    setToken(null);
    setMe(null);
  }

  return (
    <View className="page">
      <Text className="brand">美月商城</Text>
      <Text className="h1">我的</Text>
      {me ? (
        <View className="card">
          <Text>已登录：{me.displayName || me.username}</Text>
          <Text className="muted">角色：{me.roles.join(", ")}</Text>
          <Button className="btn ghost" onClick={logout}>
            退出登录
          </Button>
        </View>
      ) : (
        <View className="card">
          <Input
            className="input"
            value={username}
            onInput={(e) => setUsername(e.detail.value)}
            placeholder="用户名"
          />
          <Input
            className="input"
            password
            value={password}
            onInput={(e) => setPassword(e.detail.value)}
            placeholder="密码"
          />
          {error ? <Text className="err">{error}</Text> : null}
          <Button className="btn" onClick={login}>
            登录
          </Button>
        </View>
      )}
    </View>
  );
}
