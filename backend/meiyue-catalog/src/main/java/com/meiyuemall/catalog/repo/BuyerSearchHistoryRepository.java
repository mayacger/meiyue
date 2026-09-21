package com.meiyuemall.catalog.repo;

import com.meiyuemall.catalog.domain.BuyerSearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BuyerSearchHistoryRepository extends JpaRepository<BuyerSearchHistory, Long> {

    List<BuyerSearchHistory> findByUserIdOrderBySearchedAtDesc(Long userId);

    Optional<BuyerSearchHistory> findByUserIdAndKeyword(Long userId, String keyword);

    void deleteByUserId(Long userId);
}
