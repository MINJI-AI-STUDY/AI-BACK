package com.aistudy.api.auth;

import jakarta.validation.Valid;
import java.util.List;
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
	private final PrivacyService privacyService;

	public AuthController(AuthService authService, PrivacyService privacyService) {
		this.authService = authService;
		this.privacyService = privacyService;
	}

	/** 로그인 요청을 처리합니다. */
	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	/** 학생 PIN 로그인 — 학교 범위 내 학생 코드와 PIN으로 인증합니다. */
	@PostMapping("/student/login")
	public StudentLoginResponse studentLogin(@Valid @RequestBody StudentLoginRequest request) {
		return authService.studentLogin(request);
	}

	@PostMapping("/refresh")
	public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return authService.refresh(request);
	}

	@PostMapping("/logout")
	public void logout(@Valid @RequestBody LogoutRequest request) {
		authService.logout(request);
	}

	/** 현재 인증된 사용자를 반환합니다. */
	@GetMapping("/me")
	public MeResponse me(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		return authService.me(authorizationHeader);
	}

	/** 현재 사용자의 개인정보 동의 상태를 조회합니다. */
	@GetMapping("/me/privacy-consent")
	public List<PrivacyConsentResponse> getPrivacyConsents(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		AuthUser user = authService.getCurrentUser(authorizationHeader);
		return privacyService.getConsents(user.userId());
	}

	/** 개인정보 동의 상태를 기록합니다. */
	@PostMapping("/me/privacy-consent")
	public PrivacyConsentResponse recordPrivacyConsent(
		@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
		@Valid @RequestBody UpdatePrivacyConsentRequest request
	) {
		AuthUser user = authService.getCurrentUser(authorizationHeader);
		return privacyService.recordConsent(user.userId(), request.consentType(), request.consented());
	}
}
