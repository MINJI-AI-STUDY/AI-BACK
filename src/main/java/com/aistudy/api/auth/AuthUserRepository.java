package com.aistudy.api.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUserEntity, String> {
	Optional<AuthUserEntity> findByLoginId(String loginId);
}
