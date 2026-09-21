package com.meiyuemall.identity.repo;

import com.meiyuemall.identity.domain.BuyerInvoiceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BuyerInvoiceProfileRepository extends JpaRepository<BuyerInvoiceProfile, Long> {

    List<BuyerInvoiceProfile> findByUserIdOrderByDefaultProfileDescIdDesc(Long userId);

    Optional<BuyerInvoiceProfile> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("update BuyerInvoiceProfile p set p.defaultProfile = false where p.userId = :userId")
    void clearDefault(@Param("userId") Long userId);
}
