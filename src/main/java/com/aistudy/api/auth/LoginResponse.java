package com.aistudy.api.auth;

public record LoginResponse(String accessToken, Role role, String displayName) {
}