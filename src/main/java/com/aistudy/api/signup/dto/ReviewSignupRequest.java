package com.aistudy.api.signup.dto;

public record ReviewSignupRequest(boolean approve, String rejectionReason, String studentCode) {
	public String normalizedReason() {
		return rejectionReason == null ? null : rejectionReason.trim();
	}

	public String normalizedStudentCode() {
		return studentCode == null ? null : studentCode.trim();
	}
}
