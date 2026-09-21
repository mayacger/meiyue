package com.meiyuemall.platform.repo;

import com.meiyuemall.platform.domain.PlatformConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 平台配置仓储 */
public interface PlatformConfigRepository extends JpaRepository<PlatformConfig, String> {

    List<PlatformConfig> findAllByOrderByConfigKeyAsc();
}
