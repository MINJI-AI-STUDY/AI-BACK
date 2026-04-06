package com.aistudy.api.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import com.aistudy.api.common.ForbiddenException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final List<AuthUser> users = List.of(
		new AuthUser("teacher-1", "teacher", "teacher123", "교사 데모", Role.TEACHER),
		new AuthUser("student-1", "student", "student123", "학생 데모", Role.STUDENT),
		new AuthUser("operator-1", "operator", "operator123", "운영자 데모", Role.OPERATOR)
	);

	/** 로그인 요청을 검증하고 토큰을 발급합니다. */
	public LoginResponse login(LoginRequest request) {
		AuthUser user = users.stream()
			.filter(candidate -> candidate.loginId().equals(request.loginId()) && candidate.password().equals(request.password()))
			.findFirst()
			.orElseThrow(() -> new AuthException("아이디 또는 비밀번호가 올바르지 않습니다."));

		return new LoginResponse(createToken(user), user.role(), user.displayName());
	}

	/** Authorization 헤더에서 현재 사용자를 복원합니다. */
	public MeResponse me(String authorizationHeader) {
		AuthUser user = getCurrentUser(authorizationHeader);
		return new MeResponse(user.userId(), user.role(), user.displayName());
	}

	/** 현재 사용자를 반환합니다. */
	public AuthUser getCurrentUser(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			throw new AuthException("인증 토큰이 필요합니다.");
		}

		try {
			String token = authorizationHeader.substring(7);
			String loginId = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
			return users.stream()
			.filter(candidate -> candidate.loginId().equals(loginId))
			.findFirst()
			.orElseThrow(() -> new AuthException("유효하지 않은 토큰입니다."));
		} catch (IllegalArgumentException exception) {
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

	/** 토큰 문자열을 생성합니다. */
	private String createToken(AuthUser user) {
		return Base64.getEncoder().encodeToString(user.loginId().getBytes(StandardCharsets.UTF_8));
	}
}