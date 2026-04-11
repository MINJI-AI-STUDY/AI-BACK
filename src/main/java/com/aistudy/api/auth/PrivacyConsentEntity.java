package com.aistudy.api.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "privacy_consents")
public class PrivacyConsentEntity {

	@Id
	private String id;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(name = "consent_type", nullable = false, length = 64)
	private String consentType;

	@Column(nullable = false)
	private boolean consented;

	@Column(name = "consented_at")
	private LocalDateTime consentedAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected PrivacyConsentEntity() {
	}

	public PrivacyConsentEntity(String userId, String consentType, boolean consented) {
		this.id = UUID.randomUUID().toString();
		this.userId = userId;
		this.consentType = consentType;
		this.consented = consented;
		if (consented) {
			this.consentedAt = LocalDateTime.now();
		}
		this.createdAt = LocalDateTime.now();
		this.updatedAt = this.createdAt;
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
			updatedAt = createdAt;
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public String getId() { return id; }
	public String getUserId() { return userId; }
	public String getConsentType() { return consentType; }
	public boolean isConsented() { return consented; }
	public LocalDateTime getConsentedAt() { return consentedAt; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }

	public void updateConsent(boolean consented) {
		this.consented = consented;
		if (consented && this.consentedAt == null) {
			this.consentedAt = LocalDateTime.now();
		} else if (!consented) {
			this.consentedAt = null;
		}
		this.updatedAt = LocalDateTime.now();
	}
}