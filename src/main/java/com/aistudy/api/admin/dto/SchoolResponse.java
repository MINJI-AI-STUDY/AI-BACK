package com.aistudy.api.admin.dto;

import com.aistudy.api.admin.School;

public record SchoolResponse(String schoolId, String name, boolean active) {
	public static SchoolResponse from(School school) {
		return new SchoolResponse(school.getId(), school.getName(), school.isActive());
	}
}
