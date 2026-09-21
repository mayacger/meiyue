package com.meiyuemall.decoration.service;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.decoration.domain.DecorationTemplate;
import com.meiyuemall.decoration.domain.StorePage;
import com.meiyuemall.decoration.domain.StorePageStatus;
import com.meiyuemall.decoration.dto.SaveDraftRequest;
import com.meiyuemall.decoration.dto.StorePageResponse;
import com.meiyuemall.decoration.dto.TemplateResponse;
import com.meiyuemall.decoration.repo.DecorationTemplateRepository;
import com.meiyuemall.decoration.repo.StorePageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 店铺装修：选模板、改主题色/楼层、草稿→发布。
 * <p>禁止直播相关楼层类型（校验 floorsJson）。</p>
 */
@Service
public class DecorationService {

    private final DecorationTemplateRepository templateRepository;
    private final StorePageRepository storePageRepository;
    private final ObjectMapper objectMapper;

    public DecorationService(
            DecorationTemplateRepository templateRepository,
            StorePageRepository storePageRepository,
            ObjectMapper objectMapper
    ) {
        this.templateRepository = templateRepository;
        this.storePageRepository = storePageRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> listTemplates() {
        return templateRepository.findAll().stream()
                .map(t -> new TemplateResponse(t.getCode(), t.getName(), t.getDescription(), t.getDefaultFloorsJson()))
                .toList();
    }

    @Transactional
    public StorePageResponse saveDraft(SaveDraftRequest request) {
        MeiyuePrincipal principal = requireSeller();
        String normalized = normalizeAndValidateFloors(request.floorsJson());
        DecorationTemplate template = templateRepository.findByCode(request.templateCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "模板不存在"));

        StorePage draft = storePageRepository
                .findByTenantIdAndStatus(principal.getTenantId(), StorePageStatus.DRAFT)
                .orElseGet(() -> {
                    StorePage p = new StorePage();
                    p.setTenantId(principal.getTenantId());
                    p.setStoreId(principal.getStoreId());
                    p.setStatus(StorePageStatus.DRAFT);
                    p.setFloorsJson(template.getDefaultFloorsJson());
                    return p;
                });
        if (draft.getStoreId() == null) {
            draft.setStoreId(principal.getStoreId());
        }
        draft.setTemplateCode(template.getCode());
        draft.setThemeColor(request.themeColor());
        draft.setFloorsJson(normalized);
        storePageRepository.save(draft);
        return toResponse(draft);
    }

    @Transactional(readOnly = true)
    public StorePageResponse getDraft() {
        MeiyuePrincipal principal = requireSeller();
        return storePageRepository.findByTenantIdAndStatus(principal.getTenantId(), StorePageStatus.DRAFT)
                .map(this::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "尚无装修草稿，请先保存"));
    }

    @Transactional
    public StorePageResponse publish() {
        MeiyuePrincipal principal = requireSeller();
        StorePage draft = storePageRepository
                .findByTenantIdAndStatus(principal.getTenantId(), StorePageStatus.DRAFT)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "无草稿可发布"));
        assertNoLive(draft.getFloorsJson());
        // 发布前再规范化一次，保证 sortOrder
        draft.setFloorsJson(normalizeAndValidateFloors(draft.getFloorsJson()));

        StorePage published = storePageRepository
                .findByTenantIdAndStatus(principal.getTenantId(), StorePageStatus.PUBLISHED)
                .orElseGet(() -> {
                    StorePage p = new StorePage();
                    p.setTenantId(principal.getTenantId());
                    p.setStoreId(draft.getStoreId());
                    p.setStatus(StorePageStatus.PUBLISHED);
                    return p;
                });
        published.setTemplateCode(draft.getTemplateCode());
        published.setThemeColor(draft.getThemeColor());
        published.setFloorsJson(draft.getFloorsJson());
        published.setPublishedAt(Instant.now());
        storePageRepository.save(published);
        return toResponse(published);
    }

    @Transactional(readOnly = true)
    public StorePageResponse getPublished(Long tenantId) {
        return storePageRepository.findByTenantIdAndStatus(tenantId, StorePageStatus.PUBLISHED)
                .map(this::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "店铺尚未发布装修"));
    }

    private MeiyuePrincipal requireSeller() {
        MeiyuePrincipal principal = SecurityUtils.requirePrincipal();
        if (principal.getTenantId() == null || principal.getStoreId() == null) {
            throw new BusinessException(ErrorCode.TENANT_REQUIRED, "需要已开店的商家身份");
        }
        return principal;
    }

    /** 硬约束 C1：装修不得引入直播组件 */
    private void assertNoLive(String floorsJson) {
        if (floorsJson == null) {
            return;
        }
        String upper = floorsJson.toUpperCase(Locale.ROOT);
        if (upper.contains("LIVE_STREAM")
                || upper.contains("\"TYPE\":\"LIVE\"")
                || floorsJson.contains("直播")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不允许直播类楼层组件");
        }
    }

    /**
     * I10：校验楼层类型白名单，并按 sortOrder 排序写回 JSON。
     */
    private String normalizeAndValidateFloors(String floorsJson) {
        assertNoLive(floorsJson);
        try {
            JsonNode root = objectMapper.readTree(floorsJson == null || floorsJson.isBlank() ? "[]" : floorsJson);
            if (!root.isArray()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "floorsJson 必须是数组");
            }
            List<ObjectNode> floors = new ArrayList<>();
            int auto = 10;
            for (JsonNode n : root) {
                if (!n.isObject()) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "楼层项必须是对象");
                }
                ObjectNode obj = (ObjectNode) n.deepCopy();
                String type = obj.path("type").asText("").trim().toUpperCase(Locale.ROOT);
                if (type.isEmpty()) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "楼层缺少 type");
                }
                if (!AllowedFloorTypes.ALLOWED.contains(type)) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST,
                            "不支持的楼层类型: " + type + "（允许: " + AllowedFloorTypes.ALLOWED + "）");
                }
                obj.put("type", type);
                if (!obj.has("sortOrder") || !obj.get("sortOrder").isNumber()) {
                    obj.put("sortOrder", auto);
                    auto += 10;
                }
                floors.add(obj);
            }
            floors.sort(Comparator.comparingInt(o -> o.get("sortOrder").asInt()));
            ArrayNode out = objectMapper.createArrayNode();
            floors.forEach(out::add);
            return objectMapper.writeValueAsString(out);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "floorsJson 解析失败: " + ex.getMessage());
        }
    }

    private StorePageResponse toResponse(StorePage page) {
        return new StorePageResponse(
                page.getId(),
                page.getTenantId(),
                page.getStoreId(),
                page.getTemplateCode(),
                page.getThemeColor(),
                page.getFloorsJson(),
                page.getStatus().name(),
                page.getPublishedAt() == null ? null : page.getPublishedAt().toString()
        );
    }
}
