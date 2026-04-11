package com.aistudy.api.auth;

public record AuthUser(
	String userId,
	String schoolId,
	String classroomId,
	String loginId,
	String password,
	String displayName,
	Role role,
	String studentCode
) {
}
