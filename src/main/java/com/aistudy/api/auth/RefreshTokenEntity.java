package com.aistudy.api.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {
	@Id
	private String id;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(nullable = false, unique = true)
	private String token;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(nullable = false)
	private boolean revoked;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected RefreshTokenEntity() {
	}

	public RefreshTokenEntity(String id, String userId, String token, LocalDateTime expiresAt) {
		this.id = id;
		this.userId = userId;
		this.token = token;
		this.expiresAt = expiresAt;
		this.revoked = false;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public String getId() { return id; }
	public String getUserId() { return userId; }
	public String getToken() { return token; }
	public LocalDateTime getExpiresAt() { return expiresAt; }
	public boolean isRevoked() { return revoked; }

	public boolean isExpired(LocalDateTime now) {
		return expiresAt.isBefore(now);
	}

	public void revoke() {
		this.revoked = true;
	}
}
