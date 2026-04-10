package com.aistudy.api.auth;

public record MeResponse(String userId, String schoolId, String classroomId, Role role, String displayName) {
}
