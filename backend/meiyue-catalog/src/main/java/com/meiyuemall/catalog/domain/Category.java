package com.meiyuemall.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "parent_id")
    private Long parentId;
    @Column(nullable = false, length = 128)
    private String name;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CategoryStatus status = CategoryStatus.ENABLED;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Long getParentId() { return parentId; }
    public String getName() { return name; }
    public int getSortOrder() { return sortOrder; }
    public CategoryStatus getStatus() { return status; }
}
