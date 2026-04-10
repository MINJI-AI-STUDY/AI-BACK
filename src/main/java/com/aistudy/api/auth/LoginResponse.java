package com.aistudy.api.auth;

public record LoginResponse(String accessToken, String refreshToken, Role role, String displayName) {
	public static LoginResponse from(TokenResponse tokenResponse) {
		return new LoginResponse(tokenResponse.accessToken(), tokenResponse.refreshToken(), tokenResponse.role(), tokenResponse.displayName());
	}
}
