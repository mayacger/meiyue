package com.meiyuemall.tenant.service;

import com.meiyuemall.common.audit.Audited;
import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import com.meiyuemall.common.security.MeiyuePrincipal;
import com.meiyuemall.common.security.SecurityUtils;
import com.meiyuemall.identity.domain.RoleCode;
import com.meiyuemall.identity.domain.UserAccount;
import com.meiyuemall.identity.domain.UserStatus;
import com.meiyuemall.identity.repo.UserAccountRepository;
import com.meiyuemall.identity.service.UserRoleService;
import com.meiyuemall.tenant.domain.MemberRole;
import com.meiyuemall.tenant.domain.SellerMember;
import com.meiyuemall.tenant.dto.InviteStaffRequest;
import com.meiyuemall.tenant.dto.StaffMemberResponse;
import com.meiyuemall.tenant.repo.SellerMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商家员工管理（I26）：店主邀请/列表/移除店员。
 * <p>店员获 SELLER_STAFF；结算与店铺设置写操作仍仅 OWNER。</p>
 */
@Service
public class StaffService {

    private final SellerMemberRepository sellerMemberRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserRoleService userRoleService;

    public StaffService(
            SellerMemberRepository sellerMemberRepository,
            UserAccountRepository userAccountRepository,
            UserRoleService userRoleService
    ) {
        this.sellerMemberRepository = sellerMemberRepository;
        this.userAccountRepository = userAccountRepository;
        this.userRoleService = userRoleService;
    }

    @Transactional(readOnly = true)
    public List<StaffMemberResponse> listMine() {
        Long tenantId = requireOwnerTenant();
        List<SellerMember> members = sellerMemberRepository.findByTenantIdOrderByCreatedAtAsc(tenantId);
        if (members.isEmpty()) {
            return List.of();
        }
        Map<Long, UserAccount> users = userAccountRepository.findAllById(
                members.stream().map(SellerMember::getUserId).toList()
        ).stream().collect(Collectors.toMap(UserAccount::getId, Function.identity()));
        List<StaffMemberResponse> result = new ArrayList<>();
        for (SellerMember m : members) {
            UserAccount u = users.get(m.getUserId());
            result.add(toResponse(m, u));
        }
        return result;
    }

    /**
     * 邀请已注册用户为本店 STAFF（不可邀请已有店铺归属的用户）。
     */
    @Transactional
    @Audited(action = "STAFF_INVITE", resourceType = "SellerMember")
    public StaffMemberResponse invite(InviteStaffRequest request) {
        Long tenantId = requireOwnerTenant();
        UserAccount user = userAccountRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在，请先注册账号"));
        if (user.getStatus() != UserStatus.ENABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "该账号已禁用");
        }
        if (sellerMemberRepository.existsByUserId(user.getId())) {
            throw new BusinessException(ErrorCode.CONFLICT, "该用户已属于某店铺，无法重复邀请");
        }
        SellerMember member = new SellerMember();
        member.setTenantId(tenantId);
        member.setUserId(user.getId());
        member.setMemberRole(MemberRole.STAFF);
        sellerMemberRepository.save(member);
        userRoleService.grantRole(user.getId(), RoleCode.SELLER_STAFF);
        return toResponse(member, user);
    }

    /**
     * 移除店员（不可移除 OWNER）。
     */
    @Transactional
    @Audited(action = "STAFF_REMOVE", resourceType = "SellerMember")
    public void remove(Long memberId) {
        Long tenantId = requireOwnerTenant();
        SellerMember member = sellerMemberRepository.findByIdAndTenantId(memberId, tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "成员不存在"));
        if (member.getMemberRole() == MemberRole.OWNER) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不可移除店主");
        }
        Long userId = member.getUserId();
        sellerMemberRepository.delete(member);
        userRoleService.revokeRole(userId, RoleCode.SELLER_STAFF);
    }

    private Long requireOwnerTenant() {
        MeiyuePrincipal p = SecurityUtils.requirePrincipal();
        if (p.getTenantId() == null) {
            throw new BusinessException(ErrorCode.TENANT_REQUIRED, "需要已开店的商家身份");
        }
        // 方法级 @PreAuthorize 已限制 OWNER；此处双保险
        return p.getTenantId();
    }

    private StaffMemberResponse toResponse(SellerMember m, UserAccount u) {
        return new StaffMemberResponse(
                m.getId(),
                m.getUserId(),
                u == null ? null : u.getUsername(),
                u == null ? null : u.getDisplayName(),
                m.getMemberRole().name(),
                m.getCreatedAt() == null ? null : m.getCreatedAt().toString()
        );
    }
}
