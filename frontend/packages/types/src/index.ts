/**
 * 美月商城共享类型
 * 与后端 ApiResponse / DTO 对齐；前端各端共用。
 */

export type ActorType = "BUYER" | "SELLER" | "PLATFORM" | "ANONYMOUS";

/** 统一响应外壳：success / code / message / data */
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

/** 用户资料：id / username / displayName / roles / tenantId / storeId / actorType */
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

/** 登录/注册响应：accessToken / expiresIn / user */
export interface AuthResult {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserProfile;
}

/** 入驻申请：shopName / shopSlug / status PENDING|APPROVED|REJECTED */
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

/** 店铺：id / tenantId / name / slug / status */
export interface StoreInfo {
  id: number;
  tenantId: number;
  name: string;
  slug: string;
  status: string;
  createdAt: string;
}

/** SKU 摘要 */
export interface SkuSummary {
  id: number;
  skuCode: string;
  specText?: string;
  priceCents: number;
  stockQty?: number;
}

/** 商品列表项 */
export interface ProductSummary {
  id: number;
  tenantId: number;
  title: string;
  subtitle?: string;
  status?: string;
  skus: SkuSummary[];
  promoVideoUrl?: string | null;
}

/** 购物车行 */
export interface CartItem {
  id: number;
  productTitle: string;
  skuCode: string;
  unitPriceCents: number;
  quantity: number;
  lineTotalCents: number;
  skuId: number;
  tenantId?: number;
}

/** 订单摘要 */
export interface OrderSummary {
  id: number;
  orderNo: string;
  status: string;
  totalCents: number;
  paymentNo?: string;
}

/** 平台/店券 */
export interface CouponSummary {
  id: number;
  code: string;
  title: string;
  discountCents: number;
  minSpendCents: number;
  totalQuota?: number;
  claimedCount?: number;
  status?: string;
}

/** 券领取记录 */
export interface CouponClaim {
  id: number;
  couponId: number;
  status: string;
}

/** 站内通知 */
export interface NotificationItem {
  id: number;
  title: string;
  body: string;
  category: string;
  read: boolean;
}

/**
 * 买家收货地址（I18）
 * 字段：id / receiverName / receiverPhone / province / city / district /
 * detailAddress / defaultAddress
 */
export interface BuyerAddress {
  id: number;
  receiverName: string;
  receiverPhone: string;
  province: string;
  city: string;
  district: string;
  detailAddress: string;
  defaultAddress: boolean;
}

/**
 * 平台用户目录项（I18）
 * 字段：id / username / displayName / phone / status / roles / createdAt
 */
export interface AdminUserSummary {
  id: number;
  username: string;
  displayName: string;
  phone: string | null;
  status: "ENABLED" | "DISABLED";
  roles: string[];
  createdAt?: string;
}
