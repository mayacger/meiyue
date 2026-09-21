/**
 * 美月商城 Taro API 客户端
 * - 使用 Taro.request，兼容 H5 / 微信小程序
 * - Token 可插拔 Storage（启动时由 app 注入）
 */

import Taro from "@tarojs/taro";
import type { ApiResponse } from "@meiyue/types";

const TOKEN_KEY = "meiyue_access_token";

export interface StorageAdapter {
  getItem(key: string): string | null;
  setItem(key: string, value: string): void;
  removeItem(key: string): void;
}

let storage: StorageAdapter = {
  getItem: () => null,
  setItem: () => undefined,
  removeItem: () => undefined
};

/** 配置 Token 存储（Taro Storage） */
export function configureApiStorage(adapter: StorageAdapter): void {
  storage = adapter;
}

export function getToken(): string | null {
  return storage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null): void {
  if (token) storage.setItem(TOKEN_KEY, token);
  else storage.removeItem(TOKEN_KEY);
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
 * 发起 API 请求
 * @param path 以 /api 开头
 * @param options method / data（JSON body）
 */
export async function apiFetch<T>(
  path: string,
  options: { method?: keyof Taro.request.Method; data?: unknown } = {}
): Promise<T> {
  const header: Record<string, string> = {
    Accept: "application/json"
  };
  if (options.data !== undefined) {
    header["Content-Type"] = "application/json";
  }
  const token = getToken();
  if (token) header.Authorization = `Bearer ${token}`;

  const res = await Taro.request({
    url: path,
    method: (options.method || "GET") as keyof Taro.request.Method,
    data: options.data,
    header
  });

  const body = res.data as ApiResponse<T>;
  if (res.statusCode >= 400 || !body?.success) {
    throw new ApiError(
      body?.code ?? "HTTP_ERROR",
      body?.message ?? `HTTP ${res.statusCode}`,
      res.statusCode
    );
  }
  return body.data;
}
