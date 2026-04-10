package com.aistudy.api.admin.dto;

import com.aistudy.api.auth.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAdminUserRequest(
	@NotBlank String schoolId,
	String classroomId,
	@NotBlank String displayName,
	@NotNull Role role,
	boolean active,
	String password
) {
}
