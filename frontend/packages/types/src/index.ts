/**
 * 美月商城共享类型（I1 扩展）
 */

export type ActorType = "BUYER" | "SELLER" | "PLATFORM" | "ANONYMOUS";

export interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string;
  data: T;
}

export interface PingPayload {
  brand: string;
  app: string;
  module: string;
  tenantId: number | null;
  actorType: ActorType;
}

/** 用户资料 */
export interface UserProfile {
  id: number;
  username: string;
  displayName: string;
  phone: string | null;
  roles: string[];
  tenantId: number | null;
  storeId: number | null;
  actorType: ActorType;
}

/** 登录/注册响应 */
export interface AuthResult {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserProfile;
}

/** 入驻申请 */
export interface OnboardingApplication {
  id: number;
  applicantUserId: number;
  shopName: string;
  shopSlug: string;
  contactName: string;
  contactPhone: string;
  status: "PENDING" | "APPROVED" | "REJECTED";
  reviewNote: string | null;
  tenantId: number | null;
  createdAt: string;
  reviewedAt: string | null;
}

/** 店铺 */
export interface StoreInfo {
  id: number;
  tenantId: number;
  name: string;
  slug: string;
  status: string;
  createdAt: string;
}
