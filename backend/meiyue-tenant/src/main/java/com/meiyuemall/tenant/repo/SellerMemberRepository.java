package com.meiyuemall.tenant.repo;

import com.meiyuemall.tenant.domain.SellerMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerMemberRepository extends JpaRepository<SellerMember, Long> {

    Optional<SellerMember> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
