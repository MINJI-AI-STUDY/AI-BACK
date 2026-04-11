package com.aistudy.api.auth;

import java.time.LocalDateTime;
import java.util.List;

public record MeResponse(
	String userId,
	String schoolId,
	String classroomId,
	Role role,
	String displayName,
	boolean active,
	LocalDateTime createdAt,
	List<PrivacyConsentResponse> privacyConsents
) {}