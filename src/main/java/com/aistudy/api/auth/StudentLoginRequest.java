package com.aistudy.api.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 학생 전용 로그인 요청 DTO.
 * 학교 범위 내 학생 실명과 PIN으로 인증합니다.
 */
public record StudentLoginRequest(
	@NotBlank String schoolId,
	@NotBlank String studentName,
	@NotBlank String pin
) {}
