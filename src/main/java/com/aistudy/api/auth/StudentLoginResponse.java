package com.aistudy.api.auth;

/**
 * 학생 전용 로그인 응답 DTO.
 * 교사/운영자 LoginResponse와 달리 schoolId, classroomId, studentCode를 포함합니다.
 */
public record StudentLoginResponse(
	String accessToken,
	String refreshToken,
	Role role,
	String displayName,
	String schoolId,
	String classroomId,
	String studentCode
) {
	public static StudentLoginResponse from(TokenResponse tokenResponse, String schoolId, String classroomId, String studentCode) {
		return new StudentLoginResponse(
			tokenResponse.accessToken(),
			tokenResponse.refreshToken(),
			tokenResponse.role(),
			tokenResponse.displayName(),
			schoolId,
			classroomId,
			studentCode
		);
	}
}