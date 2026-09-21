/**
 * 美月商城前端 API 客户端
 *
 * 能力：
 * - 统一 Bearer Token（可插拔 Storage：Web localStorage / Taro Storage）
 * - 解析后端 ApiResponse 外壳
 *
 * 字段说明（Token）：
 * - storageKey：默认 meiyue_access_token
 * - StorageAdapter.get/set：读写字符串或 null（清除）
 */

import type { ApiResponse } from "@meiyue/types";

/** Token 持久化适配器：Web 用 localStorage，Taro 用 Taro.get/setStorageSync */
export interface StorageAdapter {
  getItem(key: string): string | null;
  setItem(key: string, value: string): void;
  removeItem(key: string): void;
}

const TOKEN_KEY = "meiyue_access_token";

/** 默认 Web localStorage；SSR/无 window 时降级内存 Map */
function createDefaultStorage(): StorageAdapter {
  if (typeof localStorage !== "undefined") {
    return {
      getItem: (k) => localStorage.getItem(k),
      setItem: (k, v) => localStorage.setItem(k, v),
      removeItem: (k) => localStorage.removeItem(k)
    };
  }
  const mem = new Map<string, string>();
  return {
    getItem: (k) => mem.get(k) ?? null,
    setItem: (k, v) => {
      mem.set(k, v);
    },
    removeItem: (k) => {
      mem.delete(k);
    }
  };
}

let storage: StorageAdapter = createDefaultStorage();

/**
 * 配置 Token 存储（Taro 端在 app 启动时调用一次）。
 * @param adapter StorageAdapter 实现
 */
export function configureApiStorage(adapter: StorageAdapter): void {
  storage = adapter;
}

export function getToken(): string | null {
  return storage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null): void {
  if (token) {
    storage.setItem(TOKEN_KEY, token);
  } else {
    storage.removeItem(TOKEN_KEY);
  }
}

export class ApiError extends Error {
  /** 业务错误码，如 UNAUTHORIZED */
  code: string;
  /** HTTP 状态码 */
  status: number;

  constructor(code: string, message: string, status: number) {
    super(message);
    this.code = code;
    this.status = status;
  }
}

/**
 * 发起 JSON API 请求。
 * @param path 以 /api 开头的路径（开发代理到 meiyue-boot）
 * @param init fetch 选项；json 字段自动序列化为 body
 */
export async function apiFetch<T>(
  path: string,
  init: RequestInit & { json?: unknown } = {}
): Promise<T> {
  const headers = new Headers(init.headers);
  headers.set("Accept", "application/json");
  if (init.json !== undefined) {
    headers.set("Content-Type", "application/json");
  }
  const token = getToken();
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const res = await fetch(path, {
    ...init,
    headers,
    body: init.json !== undefined ? JSON.stringify(init.json) : init.body
  });

  const body = (await res.json().catch(() => null)) as ApiResponse<T> | null;
  if (!res.ok || !body?.success) {
    throw new ApiError(
      body?.code ?? "HTTP_ERROR",
      body?.message ?? `HTTP ${res.status}`,
      res.status
    );
  }
  return body.data;
}
