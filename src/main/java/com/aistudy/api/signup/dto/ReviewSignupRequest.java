package com.aistudy.api.signup.dto;

public record ReviewSignupRequest(boolean approve, String rejectionReason) {
	public String normalizedReason() {
		return rejectionReason == null ? null : rejectionReason.trim();
	}
}
