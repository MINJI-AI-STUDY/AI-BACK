package com.aistudy.api.signup.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateTeacherSignupRequest(
	@NotBlank String schoolId,
	@NotBlank String displayName,
	@NotBlank String loginId,
	@NotBlank String password,
	@Email @NotBlank String schoolEmail,
	boolean consentTerms,
	boolean consentPrivacy,
	boolean consentStudentNotice
) {}
