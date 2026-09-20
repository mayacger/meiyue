package com.meiyuemall.boot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I7 串租集成测试：商家 A 的商品，商家 B 更新应 404（租户过滤为空），列表互不可见对方草稿。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TenantIsolationIT {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ObjectMapper objectMapper;

    private String suffix;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        // 测试放宽限流，避免注册/登录被挡
        registry.add("meiyue.rate-limit.enabled", () -> "false");
    }

    @BeforeEach
    void init() {
        suffix = UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void sellerCannotUpdateOtherTenantProduct() throws Exception {
        String adminToken = login("admin", "admin123");

        String sellerA = "seller_a_" + suffix;
        String sellerB = "seller_b_" + suffix;
        onboardSeller(sellerA, "店铺A-" + suffix, adminToken);
        onboardSeller(sellerB, "店铺B-" + suffix, adminToken);

        String tokenA = login(sellerA, "pass1234");
        String tokenB = login(sellerB, "pass1234");

        // A 创建商品
        java.util.HashMap<String, Object> createBody = new java.util.HashMap<>();
        createBody.put("categoryId", 1);
        createBody.put("title", "仅A可见-" + suffix);
        createBody.put("subtitle", "iso");
        createBody.put("detailHtml", "<p>a</p>");
        createBody.put("skus", List.of(Map.of(
                "skuCode", "SKU-A-" + suffix,
                "specText", "默认",
                "priceCents", 1000,
                "stockQty", 5
        )));
        JsonNode created = postJson("/api/v1/seller/products", tokenA, createBody);
        assertTrue(created.path("success").asBoolean());
        long productId = created.path("data").path("id").asLong();
        assertTrue(productId > 0);

        // B 尝试更新 A 的商品 → NOT_FOUND（串租表现为空/不存在，而非泄露）
        ResponseEntity<String> updateResp = exchange(
                HttpMethod.PUT,
                "/api/v1/seller/products/" + productId,
                tokenB,
                Map.of(
                        "categoryId", 1,
                        "title", "劫持标题",
                        "subtitle", "x",
                        "detailHtml", "<p>hack</p>",
                        "skus", List.of(Map.of(
                                "skuCode", "HACK",
                                "specText", "x",
                                "priceCents", 1,
                                "stockQty", 1
                        ))
                )
        );
        assertEquals(HttpStatus.NOT_FOUND, updateResp.getStatusCode());
        JsonNode updateBody = objectMapper.readTree(updateResp.getBody());
        assertEquals("NOT_FOUND", updateBody.path("code").asText());

        // B 的商品列表不应包含 A 的标题
        JsonNode listB = getJson("/api/v1/seller/products", tokenB);
        assertTrue(listB.path("success").asBoolean());
        String listText = listB.path("data").toString();
        assertFalse(listText.contains("仅A可见-" + suffix), "商家 B 列表泄露了商家 A 商品");
    }

    @Test
    void sellerCannotAttachAiCoverToOtherTenantProduct() throws Exception {
        String adminToken = login("admin", "admin123");
        String sellerA = "seller_c_" + suffix;
        String sellerB = "seller_d_" + suffix;
        onboardSeller(sellerA, "店C-" + suffix, adminToken);
        onboardSeller(sellerB, "店D-" + suffix, adminToken);
        String tokenA = login(sellerA, "pass1234");
        String tokenB = login(sellerB, "pass1234");

        JsonNode created = postJson("/api/v1/seller/products", tokenA, Map.of(
                "title", "AI隔离-" + suffix,
                "skus", List.of(Map.of(
                        "skuCode", "SKU-C-" + suffix,
                        "priceCents", 2000,
                        "stockQty", 3
                ))
        ));
        long productId = created.path("data").path("id").asLong();

        ResponseEntity<String> resp = exchange(
                HttpMethod.POST,
                "/api/v1/seller/ai/products/" + productId + "/cover",
                tokenB,
                Map.of("prompt", "玫瑰花束")
        );
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        JsonNode body = objectMapper.readTree(resp.getBody());
        assertEquals("NOT_FOUND", body.path("code").asText());
    }

    private void onboardSeller(String username, String shopName, String adminToken) throws Exception {
        postJson("/api/v1/auth/register", null, Map.of(
                "username", username,
                "password", "pass1234",
                "displayName", username,
                "role", "SELLER_OWNER"
        ));
        String token = login(username, "pass1234");
        String slug = username.toLowerCase().replace("_", "-");
        JsonNode app = postJson("/api/v1/seller/onboarding/apply", token, Map.of(
                "shopName", shopName,
                "shopSlug", slug,
                "contactName", username,
                "contactPhone", "13800000000"
        ));
        long appId = app.path("data").path("id").asLong();
        postJson("/api/v1/admin/onboarding/" + appId + "/approve", adminToken, Map.of("reviewNote", "ok"));
    }

    private String login(String username, String password) throws Exception {
        JsonNode body = postJson("/api/v1/auth/login", null, Map.of(
                "username", username,
                "password", password
        ));
        assertTrue(body.path("success").asBoolean(), body.toString());
        return body.path("data").path("accessToken").asText();
    }

    private JsonNode postJson(String path, String token, Object body) throws Exception {
        ResponseEntity<String> resp = exchange(HttpMethod.POST, path, token, body);
        assertTrue(resp.getStatusCode().is2xxSuccessful(), path + " -> " + resp.getStatusCode() + " " + resp.getBody());
        return objectMapper.readTree(resp.getBody());
    }

    private JsonNode getJson(String path, String token) throws Exception {
        ResponseEntity<String> resp = exchange(HttpMethod.GET, path, token, null);
        assertTrue(resp.getStatusCode().is2xxSuccessful(), resp.getBody());
        return objectMapper.readTree(resp.getBody());
    }

    private ResponseEntity<String> exchange(HttpMethod method, String path, String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        HttpEntity<?> entity = body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
        return rest.exchange(path, method, entity, String.class);
    }
}
