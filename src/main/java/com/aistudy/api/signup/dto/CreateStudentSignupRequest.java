package com.aistudy.api.signup.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateStudentSignupRequest(
	@NotBlank String schoolId,
	String classroomId,
	@NotBlank String realName,
	boolean consentTerms,
	boolean consentPrivacy,
	boolean consentStudentNotice
) {}
