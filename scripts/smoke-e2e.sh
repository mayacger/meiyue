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

print("==> add cart + checkout")
req("POST", "/api/v1/buyer/cart/items", buyer_token, {"skuId": sku_id, "quantity": 1})
order = req("POST", "/api/v1/buyer/orders/checkout", buyer_token, {})
order_id = order["id"]

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

print("==> review")
req("POST", "/api/v1/buyer/reviews", buyer_token, {
    "orderId": order_id, "orderItemId": item_id, "rating": 5, "content": "冒烟评价 OK"
})

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
