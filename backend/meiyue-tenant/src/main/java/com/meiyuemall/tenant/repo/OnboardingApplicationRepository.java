package com.meiyuemall.tenant.repo;

import com.meiyuemall.tenant.domain.OnboardingApplication;
import com.meiyuemall.tenant.domain.OnboardingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OnboardingApplicationRepository extends JpaRepository<OnboardingApplication, Long> {

    List<OnboardingApplication> findByStatusOrderByCreatedAtAsc(OnboardingStatus status);

    Optional<OnboardingApplication> findFirstByApplicantUserIdOrderByCreatedAtDesc(Long applicantUserId);

    boolean existsByApplicantUserIdAndStatus(Long applicantUserId, OnboardingStatus status);

    boolean existsByShopSlug(String shopSlug);
}
