package com.aistudy.api.signup.dto;

import jakarta.validation.constraints.NotBlank;

/** 학생 가입 요청 DTO — PIN 필드 포함 */
public record CreateStudentSignupRequest(
	@NotBlank String schoolId,
	String classroomId,
	@NotBlank String realName,
	@NotBlank String pin,
	boolean consentTerms,
	boolean consentPrivacy,
	boolean consentStudentNotice
) {}
