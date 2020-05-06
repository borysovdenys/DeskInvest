package com.borysov.dev.configurations.auditable;

import com.borysov.dev.models.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfiguration {

	@Bean
	public AuditorAware<UUID> auditorProvider() {
		return this::getCurrentAuditor;
	}

	private Optional<UUID> getCurrentAuditor() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return Optional.of(user.getUuid());
	}

	public UUID getCurrentAuditorUUID() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return Optional.of(user.getUuid()).orElse(null);
	}
}
