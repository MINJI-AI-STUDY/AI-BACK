package com.aistudy.api.auth;

import com.aistudy.api.common.ForbiddenException;
import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final AuthUserRepository authUserRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final PrivacyService privacyService;

	public AuthService(AuthUserRepository authUserRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, PrivacyService privacyService) {
		this.authUserRepository = authUserRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.privacyService = privacyService;
	}

	/** 로그인 요청을 검증하고 토큰을 발급합니다. */
	@Transactional
	public LoginResponse login(LoginRequest request) {
		AuthUser user = authUserRepository.findByLoginId(request.loginId())
			.filter(AuthUserEntity::isActive)
			.filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPassword()))
			.map(AuthUserEntity::toAuthUser)
			.orElseThrow(() -> new AuthException("아이디 또는 비밀번호가 올바르지 않습니다."));

		return LoginResponse.from(issueTokens(user));
	}

	/** 학생 PIN 로그인 — 학교 범위 내 학생 실명과 PIN으로 인증합니다. */
	@Transactional
	public StudentLoginResponse studentLogin(StudentLoginRequest request) {
		AuthUserEntity entity = authUserRepository.findBySchoolIdAndDisplayNameAndRole(request.schoolId(), request.studentName(), Role.STUDENT)
			.filter(AuthUserEntity::isActive)
			.filter(candidate -> candidate.getPin() != null && passwordEncoder.matches(request.pin(), candidate.getPin()))
			.orElseThrow(() -> new AuthException("학교, 이름 또는 PIN이 올바르지 않습니다."));

		AuthUser user = entity.toAuthUser();
		return StudentLoginResponse.from(issueTokens(user), user.schoolId(), user.classroomId());
	}

	@Transactional
	public TokenResponse refresh(RefreshTokenRequest request) {
		RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
			.orElseThrow(() -> new AuthException("유효하지 않은 refresh token입니다."));
		if (refreshToken.isRevoked() || refreshToken.isExpired(LocalDateTime.now())) {
			throw new AuthException("만료되었거나 취소된 refresh token입니다.");
		}

		AuthUser user = authUserRepository.findById(refreshToken.getUserId())
			.filter(AuthUserEntity::isActive)
			.map(AuthUserEntity::toAuthUser)
			.orElseThrow(() -> new AuthException("사용자를 찾을 수 없습니다."));

		refreshToken.revoke();
		refreshTokenRepository.save(refreshToken);
		return issueTokens(user);
	}

	@Transactional
	public void logout(LogoutRequest request) {
		refreshTokenRepository.findByToken(request.refreshToken()).ifPresent(token -> {
			token.revoke();
			refreshTokenRepository.save(token);
		});
	}

	/** Authorization 헤더에서 현재 사용자를 복원합니다. */
	public MeResponse me(String authorizationHeader) {
		AuthUser user = getCurrentUser(authorizationHeader);
		AuthUserEntity entity = authUserRepository.findByLoginId(user.loginId())
			.orElseThrow(() -> new AuthException("유효하지 않은 토큰입니다."));
		List<PrivacyConsentResponse> consents = privacyService.getConsents(user.userId());
		return new MeResponse(user.userId(), user.schoolId(), user.classroomId(), user.role(), user.displayName(), entity.isActive(), entity.getCreatedAt(), consents);
	}

	/** 현재 사용자를 반환합니다. */
	public AuthUser getCurrentUser(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			throw new AuthException("인증 토큰이 필요합니다.");
		}

		try {
			String token = authorizationHeader.substring(7);
			String loginId = jwtTokenProvider.getLoginId(token);
			return authUserRepository.findByLoginId(loginId)
				.filter(AuthUserEntity::isActive)
				.map(AuthUserEntity::toAuthUser)
			.orElseThrow(() -> new AuthException("유효하지 않은 토큰입니다."));
		} catch (JwtException | IllegalArgumentException exception) {
			throw new AuthException("유효하지 않은 토큰입니다.");
		}
	}

	/** 현재 사용자 역할을 검증합니다. */
	public AuthUser requireRole(String authorizationHeader, Role role) {
		AuthUser user = getCurrentUser(authorizationHeader);
		if (user.role() != role) {
			throw new ForbiddenException("해당 기능에 접근할 권한이 없습니다.");
		}
		return user;
	}

	private TokenResponse issueTokens(AuthUser user) {
		revokeActiveRefreshTokens(user.userId());
		String accessToken = jwtTokenProvider.createToken(user);
		String refreshToken = UUID.randomUUID().toString();
		refreshTokenRepository.save(new RefreshTokenEntity(
			UUID.randomUUID().toString(),
			user.userId(),
			refreshToken,
			LocalDateTime.now().plusDays(7)
		));
		return TokenResponse.from(accessToken, refreshToken, user);
	}

	private void revokeActiveRefreshTokens(String userId) {
		refreshTokenRepository.findByUserIdAndRevokedFalse(userId).forEach(token -> {
			token.revoke();
			refreshTokenRepository.save(token);
		});
	}

}
