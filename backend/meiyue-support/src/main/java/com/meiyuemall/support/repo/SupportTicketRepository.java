package com.meiyuemall.support.repo;

import com.meiyuemall.support.domain.SupportTicket;
import com.meiyuemall.support.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** 客服工单仓储 */
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SupportTicket> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    Optional<SupportTicket> findByIdAndUserId(Long id, Long userId);

    Optional<SupportTicket> findByIdAndTenantId(Long id, Long tenantId);
}
