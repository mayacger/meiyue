/**
 * 美月商城前端 API 客户端（I1）
 * - 统一 Bearer Token
 * - 解析后端 ApiResponse 外壳
 */

import type { ApiResponse } from "@meiyue/types";

const TOKEN_KEY = "meiyue_access_token";

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

export class ApiError extends Error {
  code: string;
  status: number;

  constructor(code: string, message: string, status: number) {
    super(message);
    this.code = code;
    this.status = status;
  }
}

/**
 * 发起 JSON API 请求。
 * @param path 以 /api 开头的路径
 * @param init fetch 选项；body 可为对象（自动 JSON 序列化）
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
