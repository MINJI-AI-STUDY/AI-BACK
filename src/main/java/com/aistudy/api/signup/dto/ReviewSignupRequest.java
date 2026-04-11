package com.aistudy.api.signup.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewSignupRequest(boolean approve, String rejectionReason, String studentCode) {
	public String normalizedReason() {
		return rejectionReason == null ? null : rejectionReason.trim();
	}
}