package com.meiyuemall.platform.repo;

import com.meiyuemall.platform.domain.PlatformBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface PlatformBannerRepository extends JpaRepository<PlatformBanner, Long> {

    List<PlatformBanner> findAllByOrderBySortOrderDescIdDesc();

    /**
     * 当前可展示：enabled 且在投放窗口内（窗口 NULL 表示不限）。
     */
    @Query("""
            select b from PlatformBanner b
            where b.enabled = true
              and (b.startAt is null or b.startAt <= :now)
              and (b.endAt is null or b.endAt >= :now)
            order by b.sortOrder desc, b.id desc
            """)
    List<PlatformBanner> findActive(Instant now);
}
