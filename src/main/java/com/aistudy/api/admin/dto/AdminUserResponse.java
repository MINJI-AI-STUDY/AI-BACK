package com.aistudy.api.admin.dto;

import com.aistudy.api.auth.AuthUserEntity;
import com.aistudy.api.auth.Role;

public record AdminUserResponse(String userId, String schoolId, String classroomId, String loginId, String displayName, Role role, boolean active) {
	public static AdminUserResponse from(AuthUserEntity user) {
		return new AdminUserResponse(user.getId(), user.getSchoolId(), user.getClassroomId(), user.getLoginId(), user.getDisplayName(), user.getRole(), user.isActive());
	}
}
