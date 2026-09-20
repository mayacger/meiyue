package com.meiyuemall.decoration.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "decoration_templates")
public class DecorationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 64)
    private String code;
    @Column(nullable = false, length = 128)
    private String name;
    @Column(length = 512)
    private String description;
    @Column(name = "default_floors_json", nullable = false, columnDefinition = "TEXT")
    private String defaultFloorsJson;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDefaultFloorsJson() { return defaultFloorsJson; }
}
