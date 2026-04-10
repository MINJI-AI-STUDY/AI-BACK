package com.aistudy.api.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
	private final SecretKey secretKey;
	private final long expirationMs;

	public JwtTokenProvider(
		@Value("${app.auth.jwt.secret}") String secret,
		@Value("${app.auth.jwt.expiration-ms}") long expirationMs
	) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String createToken(AuthUser user) {
		Instant now = Instant.now();
		return Jwts.builder()
			.subject(user.loginId())
			.claim("userId", user.userId())
			.claim("role", user.role().name())
			.claim("schoolId", user.schoolId())
			.claim("classroomId", user.classroomId())
			.claim("displayName", user.displayName())
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plusMillis(expirationMs)))
			.signWith(secretKey)
			.compact();
	}

	public String getLoginId(String token) {
		return parseClaims(token).getSubject();
	}

	private Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
	}
}
