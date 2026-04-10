package com.aistudy.api.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {
	Optional<RefreshTokenEntity> findByToken(String token);
	List<RefreshTokenEntity> findByUserIdAndRevokedFalse(String userId);
}
