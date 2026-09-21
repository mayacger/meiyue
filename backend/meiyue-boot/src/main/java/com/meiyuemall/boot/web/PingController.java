package com.meiyuemall.boot.web;

import com.meiyuemall.common.security.SecurityConstants;
import com.meiyuemall.common.tenant.TenantContext;
import com.meiyuemall.common.web.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 脚手架探活接口。
 * <p>
 * 入口：{@code GET /api/v1/ping}
 * 用途：验证应用启动、Security 放行、租户过滤器是否注入上下文。
 * </p>
 */
@RestController
@RequestMapping(SecurityConstants.API_PREFIX)
public class PingController {

    /**
     * 返回品牌标识与当前线程 TenantContext 快照。
     */
    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping() {
        TenantContext.Holder holder = TenantContext.get();
        Map<String, Object> payload = new LinkedHashMap<>();
        // brand：对外品牌展示名
        payload.put("brand", "美月商城");
        // app：工程标识
        payload.put("app", "meiyue-mall");
        // module：当前进程角色
        payload.put("module", "boot");
        // tenantId：当前租户，可为 null
        payload.put("tenantId", holder.tenantId());
        // actorType：参与者类型枚举名
        payload.put("actorType", holder.actorType().name());
        return ApiResponse.ok(payload);
    }
}
