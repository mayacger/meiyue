# 演示种子说明（I13）

## 入口

| 方式 | 命令 / 配置 |
|------|-------------|
| Spring Profile | `java -jar meiyue-boot-*.jar --spring.profiles.active=demo` |
| 环境变量 | `SPRING_PROFILES_ACTIVE=demo` |
| 属性开关 | 任意 profile 下加 `meiyue.demo.enabled=true` |

实现类：`com.meiyuemall.boot.config.DemoSeeder`（`ApplicationRunner`，幂等键 `seller1`）。

类目与装修模板仍由 Flyway `V2` 预置；本目录**不**提供可重复执行的 SQL 插入用户（密码需 BCrypt，故走 Java 种子）。

## 写入内容

```
PlatformAdminSeeder (始终)
    └── admin / admin123

DemoSeeder (meiyue.demo.enabled=true)
    ├── buyer1 / buyer123          → BUYER
    ├── seller1 / seller123        → BUYER + SELLER_OWNER
    ├── Tenant + Store(demo-flower)
    ├── Product ×2 (ON_SALE + SKU)
    └── StorePage (PUBLISHED)
```

## 关系图

```
User(seller1) ──SellerMember(OWNER)──► Tenant ──1:1──► Store(demo-flower)
                                              │
                                              ├── Product(ON_SALE) ×2
                                              └── StorePage(PUBLISHED)
User(buyer1)  → 仅 BUYER，可登录买家端下单（配合 MOCK 支付）
```

## 注意

- **禁止**在本目录或 Flyway 中提交真实支付/AI 密钥。
- 生产环境勿启用 `demo` profile。
