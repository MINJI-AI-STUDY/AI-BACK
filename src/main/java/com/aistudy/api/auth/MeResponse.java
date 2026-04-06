package com.aistudy.api.auth;

public record MeResponse(String userId, Role role, String displayName) {
}