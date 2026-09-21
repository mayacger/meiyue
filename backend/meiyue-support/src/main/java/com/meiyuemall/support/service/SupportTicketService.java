package com.meiyuemall.support.service;

import com.meiyuemall.common.audit.Audited;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.common.tenant.ActorType;
import com.meiyuemall.support.domain.SupportTicket;
import com.meiyuemall.support.domain.TicketStatus;
import com.meiyuemall.support.dto.CreateTicketRequest;
import com.meiyuemall.support.dto.TicketResponse;
import com.meiyuemall.support.repo.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 客服工单服务（I21 MVP，非 IM）。
 */
@Service
public class SupportTicketService {

    private final SupportTicketRepository ticketRepository;

    public SupportTicketService(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    @Audited(action = "TICKET_CREATE", resourceType = "SupportTicket")
    public TicketResponse create(CreateTicketRequest request) {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        SupportTicket t = new SupportTicket();
        t.setTicketNo("TK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4));
        t.setUserId(userId);
        t.setTenantId(request.tenantId());
        t.setSubject(request.subject().trim());
        t.setBody(request.body().trim());
        t.setCategory(request.category() == null || request.category().isBlank()
                ? "GENERAL" : request.category().trim().toUpperCase());
        t.setStatus(TicketStatus.OPEN);
        ticketRepository.save(t);
        return toResponse(t);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listMine() {
        Long userId = SecurityUtils.requirePrincipal().getUserId();
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listForSeller() {
        Long tenantId = requireSellerTenant();
        return ticketRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listForAdmin() {
        return ticketRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    @Audited(action = "TICKET_SELLER_REPLY", resourceType = "SupportTicket")
    public TicketResponse sellerReply(Long id, String reply) {
        Long tenantId = requireSellerTenant();
        SupportTicket t = ticketRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "工单不存在"));
        if (t.getStatus() == TicketStatus.CLOSED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "工单已关闭");
        }
        t.setSellerReply(reply == null ? "" : reply.trim());
        t.setStatus(TicketStatus.REPLIED);
        t.setRepliedAt(Instant.now());
        return toResponse(t);
    }

    @Transactional
    @Audited(action = "TICKET_ADMIN_REPLY", resourceType = "SupportTicket")
    public TicketResponse adminReply(Long id, String reply) {
        SupportTicket t = ticketRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "工单不存在"));
        if (t.getStatus() == TicketStatus.CLOSED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "工单已关闭");
        }
        t.setAdminReply(reply == null ? "" : reply.trim());
        t.setStatus(TicketStatus.REPLIED);
        t.setRepliedAt(Instant.now());
        return toResponse(t);
    }

    @Transactional
    @Audited(action = "TICKET_CLOSE", resourceType = "SupportTicket")
    public TicketResponse close(Long id) {
        SupportTicket t = ticketRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "工单不存在"));
        MeiyuePrincipal p = SecurityUtils.requirePrincipal();
        boolean owner = p.getUserId().equals(t.getUserId());
        boolean seller = p.getTenantId() != null && p.getTenantId().equals(t.getTenantId());
        boolean admin = p.getActorType() == ActorType.PLATFORM;
        if (!owner && !seller && !admin) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权关闭该工单");
        }
        t.setStatus(TicketStatus.CLOSED);
        t.setClosedAt(Instant.now());
        return toResponse(t);
    }

    private Long requireSellerTenant() {
        MeiyuePrincipal p = SecurityUtils.requirePrincipal();
        if (p.getTenantId() == null) {
            throw new BusinessException(ErrorCode.TENANT_REQUIRED);
        }
        return p.getTenantId();
    }

    private TicketResponse toResponse(SupportTicket t) {
        return new TicketResponse(
                t.getId(),
                t.getTicketNo(),
                t.getUserId(),
                t.getTenantId(),
                t.getSubject(),
                t.getBody(),
                t.getCategory(),
                t.getStatus().name(),
                t.getSellerReply(),
                t.getAdminReply(),
                t.getRepliedAt() == null ? null : t.getRepliedAt().toString(),
                t.getClosedAt() == null ? null : t.getClosedAt().toString(),
                t.getCreatedAt() == null ? null : t.getCreatedAt().toString()
        );
    }
}
