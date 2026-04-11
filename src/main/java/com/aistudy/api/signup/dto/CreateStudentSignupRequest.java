package com.aistudy.api.signup.dto;

import jakarta.validation.constraints.NotBlank;

/** 학생 가입 요청 DTO — PIN 필드 포함, studentCode는 선택 사항 */
public record CreateStudentSignupRequest(
	@NotBlank String schoolId,
	String classroomId,
	@NotBlank String realName,
	/** 학교 범위 내 고유 학생 코드 (선택, 미제공 시 운영자가 승인 시 지정) */
	String studentCode,
	@NotBlank String pin,
	boolean consentTerms,
	boolean consentPrivacy,
	boolean consentStudentNotice
) {}