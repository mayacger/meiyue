#!/usr/bin/env bash
# ============================================================
# 美月商城 · I18–I35 轻量冒烟
# 覆盖：类目 / 地址簿 / 用户 / 库存 / 概览 / 改密资料 / 审计 / 批量上下架 /
#       库存预警 / 通知 / 员工 / OpenAPI / series / 草稿 / Banner / 结算 /
#       凭证图+退货物流 / 相关推荐 / 足迹 / 评价审核 / 运费模板 /
#       取消订单 / 搜索历史 / 自动确认 / 发票抬头+订单备注
#       售后凭证与退货物流
# 前置：API 已启动（demo profile）；依赖 curl、python3
# 用法：BASE_URL=http://localhost:8080 ./scripts/smoke-i18.sh
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
admin_token = req("POST", "/api/v1/auth/login", body={"username": "admin", "password": "admin123"})["accessToken"]

print("==> admin dashboard")
dash = req("GET", "/api/v1/admin/dashboard", admin_token)
assert "pendingOnboardingCount" in dash and "buyerUserCount" in dash

print("==> category create/update/status")
cat = req("POST", "/api/v1/admin/categories", admin_token, {
    "name": f"i18类目{SUFFIX}", "parentId": None, "sortOrder": 50
})
req("PUT", f"/api/v1/admin/categories/{cat['id']}", admin_token, {
    "name": f"i18类目改{SUFFIX}", "parentId": None, "sortOrder": 51
})
req("POST", f"/api/v1/admin/categories/{cat['id']}/status", admin_token, {"status": "DISABLED"})
req("POST", f"/api/v1/admin/categories/{cat['id']}/status", admin_token, {"status": "ENABLED"})

print("==> users list")
users = req("GET", "/api/v1/admin/users", admin_token)
assert isinstance(users, list) and len(users) >= 1

seller = f"i18_s_{SUFFIX}"
buyer = f"i18_b_{SUFFIX}"
req("POST", "/api/v1/auth/register", body={
    "username": seller, "password": PASS, "displayName": seller, "role": "SELLER_OWNER"
})
req("POST", "/api/v1/auth/register", body={
    "username": buyer, "password": PASS, "displayName": buyer, "role": "BUYER"
})
seller_token = req("POST", "/api/v1/auth/login", body={"username": seller, "password": PASS})["accessToken"]
buyer_token = req("POST", "/api/v1/auth/login", body={"username": buyer, "password": PASS})["accessToken"]

print("==> onboarding + approve")
app = req("POST", "/api/v1/seller/onboarding/apply", seller_token, {
    "shopName": f"i18店{SUFFIX}", "shopSlug": f"i18-{SUFFIX}",
    "contactName": "李四", "contactPhone": "13800002222"
})
req("POST", f"/api/v1/admin/onboarding/{app['id']}/approve", admin_token, {"reviewNote": "i18 ok"})
seller_token = req("POST", "/api/v1/auth/login", body={"username": seller, "password": PASS})["accessToken"]

print("==> product + inventory stock adjust")
cat_id = req("GET", "/api/v1/categories")[0]["id"]
prod = req("POST", "/api/v1/seller/products", seller_token, {
    "categoryId": cat_id, "title": f"i18货{SUFFIX}", "subtitle": "x",
    "detailHtml": "<p>x</p>",
    "skus": [{"skuCode": f"I18-{SUFFIX}", "specText": "默认", "priceCents": 1200, "stockQty": 8}]
})
sku_id = prod["skus"][0]["id"]
inv = req("GET", "/api/v1/seller/inventory", seller_token)
assert any(x["skuId"] == sku_id for x in inv)
adj = req("POST", f"/api/v1/seller/inventory/skus/{sku_id}/stock", seller_token, {"stockQty": 42})
assert adj["stockQty"] == 42

print("==> seller dashboard")
sd = req("GET", "/api/v1/seller/dashboard", seller_token)
assert "pendingShipCount" in sd and "todaySalesCents" in sd

print("==> I24 profile + password + audit")
req("PUT", "/api/v1/auth/profile", seller_token, {
    "displayName": f"店主{SUFFIX}", "phone": "13800004444"
})
req("POST", "/api/v1/auth/password", seller_token, {
    "oldPassword": PASS, "newPassword": PASS + "y"
})
seller_token = req("POST", "/api/v1/auth/login", body={"username": seller, "password": PASS + "y"})["accessToken"]
req("POST", "/api/v1/auth/password", seller_token, {
    "oldPassword": PASS + "y", "newPassword": PASS
})
seller_token = req("POST", "/api/v1/auth/login", body={"username": seller, "password": PASS})["accessToken"]
audits = req("GET", "/api/v1/admin/audit-logs?page=0&size=3", admin_token)
assert isinstance(audits.get("content"), list)

print("==> I25 batch + alerts + notify")
req("POST", "/api/v1/seller/products/batch-status", seller_token, {
    "productIds": [prod["id"]], "status": "OFF_SALE"
})
req("POST", "/api/v1/seller/products/batch-status", seller_token, {
    "productIds": [prod["id"]], "status": "ON_SALE"
})
req("POST", f"/api/v1/seller/inventory/skus/{sku_id}/stock", seller_token, {"stockQty": 2})
alerts = req("GET", "/api/v1/seller/inventory/alerts?threshold=5", seller_token)
assert any(x["skuId"] == sku_id for x in alerts)
req("GET", "/api/v1/notifications/unread-count", buyer_token)
req("POST", "/api/v1/notifications/read-all", buyer_token)

print("==> buyer addresses CRUD")
addr = req("POST", "/api/v1/buyer/addresses", buyer_token, {
    "receiverName": "收件人", "receiverPhone": "13900003333",
    "province": "上海市", "city": "上海市", "district": "徐汇区",
    "detailAddress": f"i18路{SUFFIX}", "defaultAddress": True
})
req("GET", "/api/v1/buyer/addresses", buyer_token)
req("POST", f"/api/v1/buyer/addresses/{addr['id']}/default", buyer_token)
req("DELETE", f"/api/v1/buyer/addresses/{addr['id']}", buyer_token)

print("==> I26 OpenAPI + staff")
with urllib.request.urlopen(BASE + "/v3/api-docs") as resp:
    docs = json.loads(resp.read().decode())
assert "openapi" in docs
staff_user = f"i18_st_{SUFFIX}"
req("POST", "/api/v1/auth/register", body={
    "username": staff_user, "password": PASS, "displayName": staff_user, "role": "BUYER"
})
req("POST", "/api/v1/seller/staff/invite", seller_token, {"username": staff_user})
staff_token = req("POST", "/api/v1/auth/login", body={"username": staff_user, "password": PASS})["accessToken"]
req("GET", "/api/v1/seller/products", staff_token)

def expect_403(method, path, token):
    headers = {"Accept": "application/json", "Authorization": f"Bearer {token}"}
    r = urllib.request.Request(BASE + path, headers=headers, method=method)
    try:
        urllib.request.urlopen(r)
        print(f"FAIL expected 403 {path}", file=sys.stderr)
        raise SystemExit(1)
    except urllib.error.HTTPError as e:
        if e.code != 403:
            print(f"FAIL expected 403 got {e.code} {path}", file=sys.stderr)
            raise SystemExit(1)

expect_403("GET", "/api/v1/seller/settlements/periods", staff_token)
expect_403("GET", "/api/v1/seller/staff", staff_token)

print("==> I27 series + drafts")
ser = req("GET", "/api/v1/seller/dashboard/series?days=7", seller_token)
assert len(ser["days"]) == 7
req("GET", "/api/v1/admin/dashboard/series?days=7", admin_token)
drafts = req("GET", "/api/v1/seller/products/drafts", seller_token)
assert isinstance(drafts, list)
# 新建商品默认草稿
d = req("POST", "/api/v1/seller/products", seller_token, {
    "categoryId": cat_id, "title": f"草稿{SUFFIX}", "subtitle": "d",
    "detailHtml": "<p>d</p>",
    "skus": [{"skuCode": f"DR-{SUFFIX}", "specText": "默认", "priceCents": 100, "stockQty": 1}]
})
drafts2 = req("GET", "/api/v1/seller/products/drafts", seller_token)
assert any(p["id"] == d["id"] for p in drafts2)

print("==> I28 banners + settlements")
req("GET", "/api/v1/banners")
bn = req("POST", "/api/v1/admin/banners", admin_token, {
    "title": f"i18Banner{SUFFIX}",
    "imageUrl": "https://picsum.photos/seed/i18b/800/200",
    "linkUrl": "/products",
    "sortOrder": 1,
    "enabled": True
})
req("DELETE", f"/api/v1/admin/banners/{bn['id']}", admin_token)
req("GET", "/api/v1/admin/settlements/periods", admin_token)
req("GET", "/api/v1/seller/settlements/periods", seller_token)

print("==> I29 evidence + reverse tracking")
req("POST", f"/api/v1/seller/products/{prod['id']}/status", seller_token, {"status": "ON_SALE"})
req("POST", f"/api/v1/seller/inventory/skus/{sku_id}/stock", seller_token, {"stockQty": 20})
req("POST", "/api/v1/buyer/cart/items", buyer_token, {"skuId": sku_id, "quantity": 1})
ordx = req("POST", "/api/v1/buyer/orders/checkout", buyer_token, {})
req("POST", f"/api/v1/buyer/orders/{ordx['id']}/mock-pay", buyer_token)
itemx = req("GET", f"/api/v1/buyer/orders/{ordx['id']}", buyer_token)["items"][0]["id"]
as_ev = req("POST", "/api/v1/buyer/aftersales", buyer_token, {
    "orderId": ordx["id"], "orderItemId": itemx, "type": "RETURN_REFUND",
    "reason": "i18 evidence", "refundCents": 200,
    "evidenceImageUrls": ["https://picsum.photos/seed/i18ev/120/120"]
})
assert as_ev.get("evidenceImageUrls") and len(as_ev["evidenceImageUrls"]) >= 1
req("POST", f"/api/v1/seller/aftersales/{as_ev['id']}/approve", seller_token, {"reviewNote": "ok"})
req("POST", f"/api/v1/buyer/aftersales/{as_ev['id']}/reverse-tracking", buyer_token, {
    "carrierCode": "YTO", "trackingNo": f"YT{SUFFIX}", "remark": "i18"
})
req("POST", f"/api/v1/seller/aftersales/{as_ev['id']}/confirm-return", seller_token)

print("==> I30 related + browse + review moderation")
rel = req("GET", f"/api/v1/products/{prod['id']}/related?limit=5")
assert isinstance(rel, list)
req("POST", f"/api/v1/buyer/browse-history/{prod['id']}", buyer_token)
bh = req("GET", "/api/v1/buyer/browse-history", buyer_token)
assert any(p["id"] == prod["id"] for p in bh)
req("DELETE", "/api/v1/buyer/browse-history", buyer_token)
# 评价审核：走一笔完整收货评价
req("POST", "/api/v1/buyer/cart/items", buyer_token, {"skuId": sku_id, "quantity": 1})
ord_r = req("POST", "/api/v1/buyer/orders/checkout", buyer_token, {})
req("POST", f"/api/v1/buyer/orders/{ord_r['id']}/mock-pay", buyer_token)
req("POST", f"/api/v1/buyer/orders/{ord_r['id']}/confirm-receipt", buyer_token)
item_r = req("GET", f"/api/v1/buyer/orders/{ord_r['id']}", buyer_token)["items"][0]["id"]
rev = req("POST", "/api/v1/buyer/reviews", buyer_token, {
    "orderId": ord_r["id"], "orderItemId": item_r, "rating": 4, "content": "i18 review"
})
req("POST", f"/api/v1/admin/reviews/{rev['id']}/hide", admin_token, {"reason": "spam"})
pub = req("GET", f"/api/v1/products/{prod['id']}/reviews")
assert all(r["id"] != rev["id"] for r in pub)
req("POST", f"/api/v1/admin/reviews/{rev['id']}/restore", admin_token)

print("==> I31 freight template")
req("PUT", "/api/v1/seller/store", seller_token, {
    "freightCents": 600, "freeShippingThresholdCents": 50000
})
store = req("GET", "/api/v1/seller/store", seller_token)
assert store["freightCents"] == 600
req("POST", "/api/v1/buyer/cart/items", buyer_token, {"skuId": sku_id, "quantity": 1})
fest = req("POST", "/api/v1/buyer/orders/freight-estimate", buyer_token, {})
assert fest["freightCents"] == 600
ord_f = req("POST", "/api/v1/buyer/orders/checkout", buyer_token, {
    "buyerRemark": "请尽快发货",
    "invoiceTitle": "冒烟个人",
    "invoiceType": "PERSONAL"
})
assert ord_f["freightCents"] == 600
assert ord_f["totalCents"] == ord_f["goodsCents"] + 600
assert ord_f.get("buyerRemark") == "请尽快发货"
assert ord_f.get("invoiceTitle") == "冒烟个人"

print("==> I32 cancel unpaid + search history + auto confirm")
# 搜索历史
req("POST", "/api/v1/buyer/search-history", buyer_token, {"keyword": f"花{SUFFIX}"})
sh = req("GET", "/api/v1/buyer/search-history", buyer_token)
assert any(k == f"花{SUFFIX}" for k in sh)
req("DELETE", "/api/v1/buyer/search-history", buyer_token)
assert req("GET", "/api/v1/buyer/search-history", buyer_token) == []
# 取消未支付 → 库存回滚
stock_before = req("GET", "/api/v1/seller/inventory", seller_token)
sku_stock = next(x["stockQty"] for x in stock_before if x["skuId"] == sku_id)
req("POST", "/api/v1/buyer/cart/items", buyer_token, {"skuId": sku_id, "quantity": 1})
ord_c = req("POST", "/api/v1/buyer/orders/checkout", buyer_token, {})
assert ord_c["status"] == "PENDING_PAYMENT"
mid = next(x["stockQty"] for x in req("GET", "/api/v1/seller/inventory", seller_token) if x["skuId"] == sku_id)
assert mid == sku_stock - 1
req("POST", f"/api/v1/buyer/orders/{ord_c['id']}/cancel", buyer_token)
assert req("GET", f"/api/v1/buyer/orders/{ord_c['id']}", buyer_token)["status"] == "CANCELLED"
after = next(x["stockQty"] for x in req("GET", "/api/v1/seller/inventory", seller_token) if x["skuId"] == sku_id)
assert after == sku_stock
# 自动确认：天数改 0 → 签收后立即完成
req("PUT", "/api/v1/admin/platform-config", admin_token, {
    "key": "auto_confirm_receipt_days", "value": "0"
})
req("POST", "/api/v1/buyer/cart/items", buyer_token, {"skuId": sku_id, "quantity": 1})
ord_a = req("POST", "/api/v1/buyer/orders/checkout", buyer_token, {})
req("POST", f"/api/v1/buyer/orders/{ord_a['id']}/mock-pay", buyer_token)
ship_a = req("POST", "/api/v1/seller/shipments", seller_token, {
    "orderId": ord_a["id"], "carrierCode": "SF", "printEwaybill": True,
    "receiverName": "买家", "receiverPhone": "", "receiverAddress": ""
})
for st in ["PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED"]:
    try:
        req("POST", f"/api/v1/seller/shipments/{ship_a['id']}/status", seller_token,
            {"status": st, "description": f"auto {st}"})
    except SystemExit:
        pass
oa = req("GET", f"/api/v1/buyer/orders/{ord_a['id']}", buyer_token)
assert oa["status"] == "COMPLETED", oa
# 恢复默认天数
req("PUT", "/api/v1/admin/platform-config", admin_token, {
    "key": "auto_confirm_receipt_days", "value": "7"
})

print("==> I33 invoice profiles")
inv = req("POST", "/api/v1/buyer/invoice-profiles", buyer_token, {
    "title": f"抬头{SUFFIX}", "taxNo": "91110000MA00TEST", "invoiceType": "COMPANY",
    "defaultProfile": True
})
invs = req("GET", "/api/v1/buyer/invoice-profiles", buyer_token)
assert any(x["id"] == inv["id"] for x in invs)
req("PUT", f"/api/v1/buyer/invoice-profiles/{inv['id']}", buyer_token, {
    "title": f"抬头改{SUFFIX}", "taxNo": "91110000MA00TEST", "invoiceType": "COMPANY",
    "defaultProfile": True
})
req("DELETE", f"/api/v1/buyer/invoice-profiles/{inv['id']}", buyer_token)

print("==> I34 gallery + promo")
req("PUT", f"/api/v1/seller/products/{prod['id']}", seller_token, {
    "categoryId": cat_id,
    "title": f"冒烟商品{SUFFIX}",
    "subtitle": "i18",
    "detailHtml": "<p>x</p>",
    "galleryImageUrls": ["https://cdn.example.com/a.jpg"],
    "promoVideoUrl": "https://cdn.example.com/v.mp4",
    "skus": [{"skuCode": f"I18-{SUFFIX}", "specText": "默认", "priceCents": 1200, "stockQty": 10}]
})
pd = req("GET", f"/api/v1/products/{prod['id']}")
assert pd.get("promoVideoUrl", "").endswith("v.mp4")
assert "https://cdn.example.com/a.jpg" in (pd.get("galleryImageUrls") or [])

print("==> I35 captcha (off by default)")
assert req("GET", "/api/v1/auth/captcha").get("enabled") is False

print("")
print("SMOKE I18–I35 PASSED")
print(f"  seller={seller} buyer={buyer} staff={staff_user} sku={sku_id} cat={cat['id']}")
PY

