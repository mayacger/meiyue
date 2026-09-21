# 美月商城 · 身份与租户（I1）模块说明
#
# 入口关系
#   AuthController          /api/v1/auth/*
#   SellerOnboardingController  /api/v1/seller/*
#   AdminOnboardingController   /api/v1/admin/onboarding/*
#
# 关系图
#
#   注册/登录 ──JWT──► SecurityContext + TenantContext
#                         │
#   入驻申请(PENDING) ────┤
#                         ▼
#   平台审核通过 ──► Tenant(1) ──► Store(1) ──► SellerMember(OWNER)
#                         └──► 授予 ROLE_SELLER_OWNER
#
# 防越权基线
#   - 客户端 tenant 头不可信；tenantId 来自 seller_members
#   - @PreAuthorize 角色校验；myStore 校验主体 tenant 与资源一致
#
# 种子账号
#   admin / admin123 （PLATFORM_ADMIN，仅开发）
#
# 验证
#   见仓库 docs/scaffold-progress.md 或 Context docs/dev-progress.md
