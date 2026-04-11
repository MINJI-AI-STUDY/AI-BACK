package com.aistudy.api.auth;

import jakarta.validation.constraints.NotBlank;

public record UpdatePrivacyConsentRequest(
	@NotBlank String consentType,
	boolean consented
) {}