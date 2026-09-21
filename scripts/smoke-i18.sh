#!/usr/bin/env bash
# ============================================================
# 美月商城 · I18/I20/I24–I27 轻量冒烟
# 覆盖：类目 / 地址簿 / 用户 / 库存 / 概览 / 改密资料 / 审计 / 批量上下架 /
#       库存预警 / 通知 / 员工邀请 / OpenAPI / 看板序列 / 草稿箱
# 前置：API 已启动（demo profile 以开放 OpenAPI）；依赖 curl、python3
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

print("")
print("SMOKE I18/I20/I24–I27 PASSED")
print(f"  seller={seller} buyer={buyer} staff={staff_user} sku={sku_id} cat={cat['id']}")
PY
