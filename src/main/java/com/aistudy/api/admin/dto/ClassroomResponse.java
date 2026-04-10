package com.aistudy.api.admin.dto;

import com.aistudy.api.admin.Classroom;

public record ClassroomResponse(String classroomId, String schoolId, String name, Integer grade) {
	public static ClassroomResponse from(Classroom classroom) {
		return new ClassroomResponse(classroom.getId(), classroom.getSchoolId(), classroom.getName(), classroom.getGrade());
	}
}
