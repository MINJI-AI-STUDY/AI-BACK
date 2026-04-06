package com.aistudy.api.auth;

public record AuthUser(String userId, String loginId, String password, String displayName, Role role) {
}