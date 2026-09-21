package com.meiyuemall.decoration.repo;

import com.meiyuemall.decoration.domain.DecorationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DecorationTemplateRepository extends JpaRepository<DecorationTemplate, Long> {
    Optional<DecorationTemplate> findByCode(String code);
}
