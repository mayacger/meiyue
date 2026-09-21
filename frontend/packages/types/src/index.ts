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

/**
 * 登录图形验证码挑战（I35）
 * - enabled：服务端开关 MEIYUE_SECURITY_CAPTCHA_ENABLED
 * - captchaId：挑战 ID（登录时回传）
 * - imageBase64：SVG data URI，可直接作 img.src
 */
export interface CaptchaChallenge {
  enabled: boolean;
  captchaId: string | null;
  imageBase64: string | null;
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

/** 店铺：id / tenantId / name / slug / description / logoUrl / status + I31 运费 */
export interface StoreInfo {
  id: number;
  tenantId: number;
  name: string;
  slug: string;
  /** 店铺简介（I22） */
  description?: string | null;
  /** Logo URL（I22 占位） */
  logoUrl?: string | null;
  status: string;
  createdAt: string;
  /** I31：默认运费（分） */
  freightCents?: number;
  /** I31：包邮门槛（分）；null=无包邮 */
  freeShippingThresholdCents?: number | null;
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
  coverImageUrl?: string | null;
  coverAssetId?: number | null;
  skus: SkuSummary[];
  promoVideoUrl?: string | null;
  /** I34：图集 URL 列表 */
  galleryImageUrls?: string[];
  /** I36：软删时间 */
  deletedAt?: string | null;
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

/** 订单摘要（I31 运费 + I33 备注发票） */
export interface OrderSummary {
  id: number;
  orderNo: string;
  status: string;
  totalCents: number;
  /** 商品应付（券后、运费前） */
  goodsCents?: number | null;
  /** 运费合计 */
  freightCents?: number;
  paymentNo?: string;
  buyerRemark?: string | null;
  invoiceTitle?: string | null;
  invoiceTaxNo?: string | null;
  invoiceType?: string | null;
  deliveredAt?: string | null;
  autoConfirmAt?: string | null;
}

/** I31：结算运费预估（对齐 FreightEstimateResponse） */
export interface FreightEstimate {
  goodsCents: number;
  freightCents: number;
  /** 商品+运费（未扣券） */
  totalCents: number;
  shops: {
    tenantId: number;
    storeName: string;
    goodsCents: number;
    freightCents: number;
    freeShipping: boolean;
    /** 包邮门槛（分）；null=无包邮 */
    thresholdCents: number | null;
  }[];
}

/** I30：商品评价（含隐藏状态，Admin 可见） */
export interface ProductReview {
  id: number;
  productId: number;
  orderId?: number;
  orderItemId?: number;
  tenantId?: number;
  rating: number;
  content: string;
  sellerReply: string | null;
  createdAt: string;
  hidden?: boolean;
  hiddenReason?: string | null;
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
  /** 店券所属租户（平台券无） */
  tenantId?: number;
}

/** 券领取记录 */
export interface CouponClaim {
  id: number;
  couponId: number;
  status: string;
}

/** 商品收藏项（I22） */
export interface FavoriteItem {
  id: number;
  productId: number;
  createdAt?: string;
}

/**
 * 平台运营配置只读（I23）
 * platformFeeRateBps：基点费率；settlementCycle：结算周期；minWithdrawCents：提现门槛（分）
 */
export interface PlatformConfigView {
  items: { key: string; value: string; description?: string; updatedAt?: string }[];
  platformFeeRateBps: string;
  settlementCycle: string;
  minWithdrawCents: string;
  /** I32：签收后自动确认天数 */
  autoConfirmReceiptDays?: string;
}

/** I33：发票抬头 */
export interface InvoiceProfile {
  id: number;
  title: string;
  taxNo?: string | null;
  invoiceType: string;
  defaultProfile: boolean;
}

/** 站内通知（与后端 NotificationResponse 对齐） */
export interface NotificationItem {
  id: number;
  /** 受众 BUYER / SELLER / PLATFORM */
  audience?: string;
  title: string;
  body: string;
  category: string;
  refType?: string | null;
  refId?: string | null;
  read: boolean;
  createdAt?: string;
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
