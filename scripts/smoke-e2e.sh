#!/usr/bin/env bash
# ============================================================
# 美月商城 · 端到端冒烟（I12）
# 覆盖：注册 → 入驻审核 → 上架 → 加购下单 → MOCK 支付 → 发货
#       → 确认收货 → 评价 → 售后（REFUND_ONLY + MOCK 通道退款）
#
# 前置：
#   1. docker compose -f docker/docker-compose.yml up -d
#      （PostgreSQL :5432 + Redis :6379）
#   2. API 已启动（见 docs/ops-runbook.md）
#   3. 依赖：curl、python3
#
# 用法：
#   BASE_URL=http://localhost:8080 ./scripts/smoke-e2e.sh
# ============================================================
set -euo pipefail

export BASE_URL="${BASE_URL:-http://localhost:8080}"
export SUFFIX="$(date +%s | tail -c 7)"
export PASS="pass1234"

echo "==> health"
curl -sf "${BASE_URL}/actuator/health" | grep -q UP
echo "OK health"

python3 <<'PY'
import json, os, sys, urllib.request, urllib.error

BASE = os.environ["BASE_URL"]
SUFFIX = os.environ["SUFFIX"]
PASS = os.environ["PASS"]

def req(method, path, token=None, body=None):
    data = None if body is None else json.dumps(body).encode()
    headers = {"Accept": "application/json"}
    if body is not None:
        headers["Content-Type"] = "application/json"
    if token:
        headers["Authorization"] = f"Bearer {token}"
    r = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(r) as resp:
            raw = resp.read().decode()
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        print(f"HTTP {e.code} {path}: {raw}", file=sys.stderr)
        raise SystemExit(1)
    d = json.loads(raw)
    if not d.get("success"):
        print(f"FAIL {method} {path}: {d}", file=sys.stderr)
        raise SystemExit(1)
    return d["data"]

print("==> admin login")
admin = req("POST", "/api/v1/auth/login", body={"username": "admin", "password": "admin123"})
admin_token = admin["accessToken"]

seller = f"sm_s_{SUFFIX}"
buyer = f"sm_b_{SUFFIX}"
shop = f"冒烟店{SUFFIX}"
slug = f"smoke-{SUFFIX}"

print(f"==> register seller {seller}")
req("POST", "/api/v1/auth/register", body={
    "username": seller, "password": PASS, "displayName": seller, "role": "SELLER_OWNER"
})
print(f"==> register buyer {buyer}")
req("POST", "/api/v1/auth/register", body={
    "username": buyer, "password": PASS, "displayName": buyer, "role": "BUYER"
})

seller_token = req("POST", "/api/v1/auth/login", body={"username": seller, "password": PASS})["accessToken"]
buyer_token = req("POST", "/api/v1/auth/login", body={"username": buyer, "password": PASS})["accessToken"]

print("==> onboarding apply")
app = req("POST", "/api/v1/seller/onboarding/apply", seller_token, {
    "shopName": shop, "shopSlug": slug, "contactName": "张三", "contactPhone": "13800001111"
})
app_id = app["id"]
print(f"==> admin approve #{app_id}")
req("POST", f"/api/v1/admin/onboarding/{app_id}/approve", admin_token, {"reviewNote": "smoke ok"})

seller_token = req("POST", "/api/v1/auth/login", body={"username": seller, "password": PASS})["accessToken"]
cat_id = req("GET", "/api/v1/categories")[0]["id"]

print("==> create + on-sale product")
prod = req("POST", "/api/v1/seller/products", seller_token, {
    "categoryId": cat_id,
    "title": f"冒烟商品{SUFFIX}",
    "subtitle": "e2e",
    "detailHtml": "<p>x</p>",
    "skus": [{"skuCode": f"SK-{SUFFIX}", "specText": "默认", "priceCents": 9900, "stockQty": 20}]
})
prod_id = prod["id"]
sku_id = prod["skus"][0]["id"]
req("POST", f"/api/v1/seller/products/{prod_id}/status", seller_token, {"status": "ON_SALE"})

print("==> I20 inventory + dashboards")
inv = req("GET", "/api/v1/seller/inventory", seller_token)
assert any(x["skuId"] == sku_id for x in inv)
req("POST", f"/api/v1/seller/inventory/skus/{sku_id}/stock", seller_token, {"stockQty": 18})
req("GET", "/api/v1/seller/dashboard", seller_token)
req("GET", "/api/v1/admin/dashboard", admin_token)

print("==> I24 profile + password")
req("PUT", "/api/v1/auth/profile", buyer_token, {
    "displayName": f"买家改{SUFFIX}", "phone": "13900009999"
})
me = req("GET", "/api/v1/auth/me", buyer_token)
assert me["displayName"] == f"买家改{SUFFIX}"
req("POST", "/api/v1/auth/password", buyer_token, {
    "oldPassword": PASS, "newPassword": PASS + "x"
})
buyer_token = req("POST", "/api/v1/auth/login", body={"username": buyer, "password": PASS + "x"})["accessToken"]
req("POST", "/api/v1/auth/password", buyer_token, {
    "oldPassword": PASS + "x", "newPassword": PASS
})
buyer_token = req("POST", "/api/v1/auth/login", body={"username": buyer, "password": PASS})["accessToken"]

print("==> I24 audit logs")
audits = req("GET", "/api/v1/admin/audit-logs?page=0&size=5", admin_token)
assert "content" in audits and "totalElements" in audits

print("==> I25 batch status + inventory alerts")
req("POST", "/api/v1/seller/products/batch-status", seller_token, {
    "productIds": [prod_id], "status": "OFF_SALE"
})
req("POST", "/api/v1/seller/products/batch-status", seller_token, {
    "productIds": [prod_id], "status": "ON_SALE"
})
req("POST", f"/api/v1/seller/inventory/skus/{sku_id}/stock", seller_token, {"stockQty": 3})
alerts = req("GET", "/api/v1/seller/inventory/alerts?threshold=5", seller_token)
assert any(x["skuId"] == sku_id for x in alerts)
req("POST", f"/api/v1/seller/inventory/skus/{sku_id}/stock", seller_token, {"stockQty": 18})

print("==> I25 notifications read")
# 发货等会写入通知；至少 unread-count / read-all 可用
uc = req("GET", "/api/v1/notifications/unread-count", buyer_token)
assert "unread" in uc
req("POST", "/api/v1/notifications/read-all", buyer_token)

print("==> I26 OpenAPI (demo profile)")
# /v3/api-docs 为 springdoc 原生 JSON，非 ApiResponse 包装
import urllib.request as ur
with ur.urlopen(BASE + "/v3/api-docs") as resp:
    docs = json.loads(resp.read().decode())
assert "openapi" in docs and "paths" in docs

print("==> I26 staff invite + OWNER-only")
staff_user = f"sm_st_{SUFFIX}"
req("POST", "/api/v1/auth/register", body={
    "username": staff_user, "password": PASS, "displayName": staff_user, "role": "BUYER"
})
invited = req("POST", "/api/v1/seller/staff/invite", seller_token, {"username": staff_user})
assert invited["memberRole"] == "STAFF"
members = req("GET", "/api/v1/seller/staff", seller_token)
assert any(m["username"] == staff_user for m in members)
staff_token = req("POST", "/api/v1/auth/login", body={"username": staff_user, "password": PASS})["accessToken"]
# 店员可看商品
req("GET", "/api/v1/seller/products", staff_token)
# 结算 / 店铺写 / 员工列表仅店主
def expect_403(method, path, token, body=None):
    data = None if body is None else json.dumps(body).encode()
    headers = {"Accept": "application/json", "Authorization": f"Bearer {token}"}
    if body is not None:
        headers["Content-Type"] = "application/json"
    r = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    try:
        urllib.request.urlopen(r)
        print(f"FAIL expected 403 {method} {path}", file=sys.stderr)
        raise SystemExit(1)
    except urllib.error.HTTPError as e:
        if e.code != 403:
            print(f"FAIL expected 403 got {e.code} {path}: {e.read().decode()}", file=sys.stderr)
            raise SystemExit(1)
expect_403("GET", "/api/v1/seller/settlements/periods", staff_token)
expect_403("PUT", "/api/v1/seller/store", staff_token, {"name": "x", "description": "", "logoUrl": ""})
expect_403("GET", "/api/v1/seller/staff", staff_token)

print("==> I27 series + drafts")
ser = req("GET", "/api/v1/seller/dashboard/series?days=7", seller_token)
assert "days" in ser and len(ser["days"]) == 7
aser = req("GET", "/api/v1/admin/dashboard/series?days=7", admin_token)
assert "days" in aser and len(aser["days"]) == 7
draft = req("POST", "/api/v1/seller/products", seller_token, {
    "categoryId": cat_id, "title": f"草稿{SUFFIX}", "subtitle": "d",
    "detailHtml": "<p>d</p>",
    "skus": [{"skuCode": f"DR-{SUFFIX}", "specText": "默认", "priceCents": 100, "stockQty": 1}]
})
drafts = req("GET", "/api/v1/seller/products/drafts", seller_token)
assert any(p["id"] == draft["id"] for p in drafts)

print("==> I28 banners + admin settlements")
banners = req("GET", "/api/v1/banners")
assert isinstance(banners, list)
bn = req("POST", "/api/v1/admin/banners", admin_token, {
    "title": f"冒烟Banner{SUFFIX}",
    "imageUrl": "https://picsum.photos/seed/smoke/800/200",
    "linkUrl": "/products",
    "sortOrder": 99,
    "enabled": True
})
banners2 = req("GET", "/api/v1/banners")
assert any(x["id"] == bn["id"] for x in banners2)
req("PUT", f"/api/v1/admin/banners/{bn['id']}", admin_token, {
    "title": f"冒烟Banner改{SUFFIX}",
    "imageUrl": bn["imageUrl"],
    "linkUrl": "/products",
    "sortOrder": 99,
    "enabled": False
})
# 禁用后公开列表不应再出现
banners3 = req("GET", "/api/v1/banners")
assert not any(x["id"] == bn["id"] for x in banners3)
req("DELETE", f"/api/v1/admin/banners/{bn['id']}", admin_token)
periods = req("GET", "/api/v1/admin/settlements/periods", admin_token)
assert isinstance(periods, list)
req("GET", "/api/v1/admin/settlements/bills", admin_token)
# 商家按周期筛选
seller_periods = req("GET", "/api/v1/seller/settlements/periods", seller_token)
if seller_periods:
    pk = seller_periods[0]["periodKey"]
    req("GET", f"/api/v1/seller/settlements/bills?periodKey={pk}", seller_token)

print("==> I29 aftersale evidence + reverse tracking")
# 再下一单用于退货退款闭环（凭证图）
req("POST", "/api/v1/buyer/cart/items", buyer_token, {"skuId": sku_id, "quantity": 1})
order3 = req("POST", "/api/v1/buyer/orders/checkout", buyer_token, {})
order3_id = order3["id"]
req("POST", f"/api/v1/buyer/orders/{order3_id}/mock-pay", buyer_token)
item3 = req("GET", f"/api/v1/buyer/orders/{order3_id}", buyer_token)["items"][0]["id"]
as_rr = req("POST", "/api/v1/buyer/aftersales", buyer_token, {
    "orderId": order3_id, "orderItemId": item3, "type": "RETURN_REFUND",
    "reason": "smoke return", "refundCents": 500,
    "evidenceImageUrls": ["https://picsum.photos/seed/ev/200/200"]
})
assert as_rr.get("evidenceImageUrls") and len(as_rr["evidenceImageUrls"]) >= 1
req("POST", f"/api/v1/seller/aftersales/{as_rr['id']}/approve", seller_token, {"reviewNote": "ok"})
req("POST", f"/api/v1/buyer/aftersales/{as_rr['id']}/reverse-tracking", buyer_token, {
    "carrierCode": "SF", "trackingNo": f"SF{SUFFIX}", "remark": "smoke"
})
req("POST", f"/api/v1/seller/aftersales/{as_rr['id']}/confirm-return", seller_token)

print("==> I18 admin categories + users")
cats = req("GET", "/api/v1/admin/categories", admin_token)
assert isinstance(cats, list) and len(cats) >= 1
new_cat = req("POST", "/api/v1/admin/categories", admin_token, {
    "name": f"冒烟类目{SUFFIX}", "parentId": None, "sortOrder": 99
})
req("POST", f"/api/v1/admin/categories/{new_cat['id']}/status", admin_token, {"status": "DISABLED"})
users = req("GET", "/api/v1/admin/users", admin_token, None)
assert any(u["username"] == buyer for u in users)
buyers_only = req("GET", f"/api/v1/admin/users?role=BUYER", admin_token)
assert all("BUYER" in (u.get("roles") or []) for u in buyers_only)

print("==> I18 buyer addresses CRUD")
addr = req("POST", "/api/v1/buyer/addresses", buyer_token, {
    "receiverName": "冒烟收件人", "receiverPhone": "13900001111",
    "province": "上海市", "city": "上海市", "district": "浦东新区",
    "detailAddress": f"冒烟路{SUFFIX}号", "defaultAddress": True
})
addrs = req("GET", "/api/v1/buyer/addresses", buyer_token)
assert any(a["id"] == addr["id"] for a in addrs)
req("PUT", f"/api/v1/buyer/addresses/{addr['id']}", buyer_token, {
    "receiverName": "冒烟收件人改", "receiverPhone": "13900001111",
    "province": "上海市", "city": "上海市", "district": "浦东新区",
    "detailAddress": f"冒烟路{SUFFIX}号-改", "defaultAddress": True
})
req("POST", f"/api/v1/buyer/addresses/{addr['id']}/default", buyer_token)

print("==> add cart + freight estimate + checkout")
req("POST", "/api/v1/buyer/cart/items", buyer_token, {"skuId": sku_id, "quantity": 1})
# I31：商家设置运费模板（默认 8 元，满 199 包邮；单价 99 未达包邮）
req("PUT", "/api/v1/seller/store", seller_token, {
    "freightCents": 800, "freeShippingThresholdCents": 19900
})
fest = req("POST", "/api/v1/buyer/orders/freight-estimate", buyer_token, {})
assert fest["freightCents"] == 800, fest
order = req("POST", "/api/v1/buyer/orders/checkout", buyer_token, {})
order_id = order["id"]
assert order.get("freightCents") == 800, order
assert order.get("goodsCents") == 9900, order
assert order["totalCents"] == 10700, order

print(f"==> mock pay #{order_id}")
req("POST", f"/api/v1/buyer/orders/{order_id}/mock-pay", buyer_token)

print("==> ship (MOCK ewaybill)")
ship = req("POST", "/api/v1/seller/shipments", seller_token, {
    "orderId": order_id, "carrierCode": "SF", "printEwaybill": True,
    "receiverName": "买家", "receiverPhone": "", "receiverAddress": ""
})
ship_id = ship["id"]
for st in ["PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED"]:
    try:
        req("POST", f"/api/v1/seller/shipments/{ship_id}/status", seller_token,
            {"status": st, "description": f"smoke {st}"})
    except SystemExit:
        pass

print("==> confirm receipt")
req("POST", f"/api/v1/buyer/orders/{order_id}/confirm-receipt", buyer_token)
item_id = req("GET", f"/api/v1/buyer/orders/{order_id}", buyer_token)["items"][0]["id"]

print("==> review + I30 related/browse/hide")
req("POST", "/api/v1/buyer/reviews", buyer_token, {
    "orderId": order_id, "orderItemId": item_id, "rating": 5, "content": "冒烟评价 OK"
})
# 相关推荐（同店至少自身被排除，可为空列表）
rel = req("GET", f"/api/v1/products/{prod_id}/related?limit=5")
assert isinstance(rel, list)
# 浏览足迹
req("POST", f"/api/v1/buyer/browse-history/{prod_id}", buyer_token)
bh = req("GET", "/api/v1/buyer/browse-history", buyer_token)
assert any(p["id"] == prod_id for p in bh)
req("DELETE", "/api/v1/buyer/browse-history", buyer_token)
bh2 = req("GET", "/api/v1/buyer/browse-history", buyer_token)
assert bh2 == []
# 评价审核：隐藏后公开列表不可见
admin_revs = req("GET", "/api/v1/admin/reviews", admin_token)
rev = next(r for r in admin_revs if r["orderItemId"] == item_id)
req("POST", f"/api/v1/admin/reviews/{rev['id']}/hide", admin_token, {"reason": "smoke hide"})
pub = req("GET", f"/api/v1/products/{prod_id}/reviews")
assert all(r["id"] != rev["id"] for r in pub)
req("POST", f"/api/v1/admin/reviews/{rev['id']}/restore", admin_token)
pub2 = req("GET", f"/api/v1/products/{prod_id}/reviews")
assert any(r["id"] == rev["id"] for r in pub2)

print("==> I32 cancel + search + auto-confirm days=0")
req("POST", "/api/v1/buyer/search-history", buyer_token, {"keyword": "玫瑰"})
assert "玫瑰" in req("GET", "/api/v1/buyer/search-history", buyer_token)
req("DELETE", "/api/v1/buyer/search-history", buyer_token)
req("POST", "/api/v1/buyer/cart/items", buyer_token, {"skuId": sku_id, "quantity": 1})
ord_cancel = req("POST", "/api/v1/buyer/orders/checkout", buyer_token, {})
req("POST", f"/api/v1/buyer/orders/{ord_cancel['id']}/cancel", buyer_token)
assert req("GET", f"/api/v1/buyer/orders/{ord_cancel['id']}", buyer_token)["status"] == "CANCELLED"
req("PUT", "/api/v1/admin/platform-config", admin_token, {
    "key": "auto_confirm_receipt_days", "value": "0"
})
req("POST", "/api/v1/buyer/cart/items", buyer_token, {"skuId": sku_id, "quantity": 1})
ord_auto = req("POST", "/api/v1/buyer/orders/checkout", buyer_token, {
    "buyerRemark": "e2e remark", "invoiceTitle": "e2e抬头", "invoiceType": "PERSONAL"
})
assert ord_auto.get("buyerRemark") == "e2e remark"
req("POST", f"/api/v1/buyer/orders/{ord_auto['id']}/mock-pay", buyer_token)
ship_auto = req("POST", "/api/v1/seller/shipments", seller_token, {
    "orderId": ord_auto["id"], "carrierCode": "SF", "printEwaybill": True,
    "receiverName": "买家", "receiverPhone": "", "receiverAddress": ""
})
for st in ["PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED"]:
    try:
        req("POST", f"/api/v1/seller/shipments/{ship_auto['id']}/status", seller_token,
            {"status": st, "description": f"auto {st}"})
    except SystemExit:
        pass
assert req("GET", f"/api/v1/buyer/orders/{ord_auto['id']}", buyer_token)["status"] == "COMPLETED"
req("PUT", "/api/v1/admin/platform-config", admin_token, {
    "key": "auto_confirm_receipt_days", "value": "7"
})

print("==> I33 invoice profile CRUD")
inv = req("POST", "/api/v1/buyer/invoice-profiles", buyer_token, {
    "title": "E2E公司", "taxNo": "91110000MA00E2E", "invoiceType": "COMPANY", "defaultProfile": True
})
req("DELETE", f"/api/v1/buyer/invoice-profiles/{inv['id']}", buyer_token)

print("==> aftersale REFUND_ONLY (MOCK channel refund)")
req("POST", "/api/v1/buyer/cart/items", buyer_token, {"skuId": sku_id, "quantity": 1})
order2 = req("POST", "/api/v1/buyer/orders/checkout", buyer_token, {})
order2_id = order2["id"]
req("POST", f"/api/v1/buyer/orders/{order2_id}/mock-pay", buyer_token)
item2 = req("GET", f"/api/v1/buyer/orders/{order2_id}", buyer_token)["items"][0]["id"]
asale = req("POST", "/api/v1/buyer/aftersales", buyer_token, {
    "orderId": order2_id, "orderItemId": item2, "type": "REFUND_ONLY",
    "reason": "smoke refund", "refundCents": 1000
})
as_id = asale["id"]
print(f"==> seller approve aftersale #{as_id}")
req("POST", f"/api/v1/seller/aftersales/{as_id}/approve", seller_token, {"reviewNote": "smoke approve"})

print("")
print("SMOKE E2E PASSED")
print(f"  seller={seller} buyer={buyer} order={order_id} aftersale={as_id}")
PY
