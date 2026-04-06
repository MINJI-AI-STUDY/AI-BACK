package com.aistudy.api.auth;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	/** 로그인 요청을 처리합니다. */
	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	/** 현재 인증된 사용자를 반환합니다. */
	@GetMapping("/me")
	public MeResponse me(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		return authService.me(authorizationHeader);
	}
}