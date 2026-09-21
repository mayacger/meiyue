package com.meiyuemall.identity.repo;

import com.meiyuemall.identity.domain.BuyerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** 买家地址仓储 */
public interface BuyerAddressRepository extends JpaRepository<BuyerAddress, Long> {

    List<BuyerAddress> findByUserIdOrderByDefaultAddressDescIdDesc(Long userId);

    Optional<BuyerAddress> findByIdAndUserId(Long id, Long userId);

    /** 清除该用户全部默认标记；需在事务内调用 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update BuyerAddress a set a.defaultAddress = false where a.userId = :userId")
    void clearDefaultForUser(@Param("userId") Long userId);
}
