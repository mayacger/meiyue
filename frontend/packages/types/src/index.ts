/**
 * 美月商城共享类型（脚手架最小集）
 * 后续可由 OpenAPI 生成补齐；本轮手写占位。
 */

/** 参与者类型，与后端 ActorType 对齐 */
export type ActorType = "BUYER" | "SELLER" | "PLATFORM" | "ANONYMOUS";

/** 统一 API 响应外壳，对应后端 ApiResponse */
export interface ApiResponse<T> {
  /** 是否成功 */
  success: boolean;
  /** 业务码，成功一般为 OK */
  code: string;
  /** 人类可读说明 */
  message: string;
  /** 业务载荷 */
  data: T;
}

/** ping 接口载荷 */
export interface PingPayload {
  /** 对外品牌名 */
  brand: string;
  /** 工程标识 */
  app: string;
  /** 进程模块 */
  module: string;
  /** 当前租户 ID，可为 null */
  tenantId: number | null;
  /** 参与者类型 */
  actorType: ActorType;
}
