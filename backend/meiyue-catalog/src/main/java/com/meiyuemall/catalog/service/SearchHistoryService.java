package com.meiyuemall.catalog.service;

import com.meiyuemall.catalog.domain.BuyerSearchHistory;
import com.meiyuemall.catalog.repo.BuyerSearchHistoryRepository;
import com.meiyuemall.common.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 买家搜索历史（I32）：记录 / 列表 / 清空。
 */
@Service
public class SearchHistoryService {

    private static final int MAX_KEEP = 20;
    private static final int MAX_KEYWORD_LEN = 64;

    private final BuyerSearchHistoryRepository repository;

    public SearchHistoryService(BuyerSearchHistoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(String rawKeyword) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        String keyword = normalize(rawKeyword);
        if (keyword.isEmpty()) {
            return;
        }
        BuyerSearchHistory row = repository.findByUserIdAndKeyword(userId, keyword)
                .orElseGet(() -> {
                    BuyerSearchHistory h = new BuyerSearchHistory();
                    h.setUserId(userId);
                    h.setKeyword(keyword);
                    return h;
                });
        row.setSearchedAt(Instant.now());
        repository.save(row);
        List<BuyerSearchHistory> all = repository.findByUserIdOrderBySearchedAtDesc(userId);
        if (all.size() > MAX_KEEP) {
            repository.deleteAll(all.subList(MAX_KEEP, all.size()));
        }
    }

    @Transactional(readOnly = true)
    public List<String> listMine() {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        return repository.findByUserIdOrderBySearchedAtDesc(userId).stream()
                .map(BuyerSearchHistory::getKeyword)
                .toList();
    }

    @Transactional
    public void clearMine() {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        repository.deleteByUserId(userId);
    }

    private static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String k = raw.trim();
        if (k.length() > MAX_KEYWORD_LEN) {
            k = k.substring(0, MAX_KEYWORD_LEN);
        }
        return k;
    }
}
