package com.aistudy.api.auth;

import java.time.LocalDateTime;

public record PrivacyConsentResponse(
	String consentType,
	boolean consented,
	LocalDateTime consentedAt,
	LocalDateTime updatedAt
) {
	public static PrivacyConsentResponse from(PrivacyConsentEntity entity) {
		return new PrivacyConsentResponse(
			entity.getConsentType(),
			entity.isConsented(),
			entity.getConsentedAt(),
			entity.getUpdatedAt()
		);
	}
}