package com.wrightlabs.marketplace.domain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.wrightlabs.marketplace.domain.repository")
@EnableJpaAuditing
public class JpaConfig {
}
