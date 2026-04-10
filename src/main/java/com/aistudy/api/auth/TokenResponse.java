package com.aistudy.api.auth;

public record TokenResponse(String accessToken, String refreshToken, Role role, String displayName) {
	public static TokenResponse from(String accessToken, String refreshToken, AuthUser user) {
		return new TokenResponse(accessToken, refreshToken, user.role(), user.displayName());
	}
}
