package com.meiyuemall.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 买家搜索关键词历史（I32）。
 * 同一用户同一关键词只保留一行，searchedAt 刷新。
 */
@Entity
@Table(name = "buyer_search_histories")
public class BuyerSearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String keyword;

    @Column(name = "searched_at", nullable = false)
    private Instant searchedAt;

    @PrePersist
    void onCreate() {
        if (searchedAt == null) {
            searchedAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Instant getSearchedAt() { return searchedAt; }
    public void setSearchedAt(Instant searchedAt) { this.searchedAt = searchedAt; }
}
