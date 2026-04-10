package com.aistudy.api.admin.dto;

import com.aistudy.api.auth.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAdminUserRequest(
	@NotBlank String schoolId,
	String classroomId,
	@NotBlank String loginId,
	@NotBlank String password,
	@NotBlank String displayName,
	@NotNull Role role
) {
}
