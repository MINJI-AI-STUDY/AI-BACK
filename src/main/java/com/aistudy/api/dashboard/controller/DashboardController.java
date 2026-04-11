package com.aistudy.api.dashboard.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.common.ForbiddenException;
import com.aistudy.api.dashboard.dto.DocumentDashboardResponse;
import com.aistudy.api.dashboard.dto.OperatorOverviewResponse;
import com.aistudy.api.dashboard.dto.TeacherDashboardResponse;
import com.aistudy.api.dashboard.service.DashboardService;
import com.aistudy.api.signup.repository.SchoolOperatorMembershipRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {
	private final AuthService authService;
	private final DashboardService dashboardService;
	private final SchoolOperatorMembershipRepository schoolOperatorMembershipRepository;

	public DashboardController(AuthService authService, DashboardService dashboardService, SchoolOperatorMembershipRepository schoolOperatorMembershipRepository) {
		this.authService = authService;
		this.dashboardService = dashboardService;
		this.schoolOperatorMembershipRepository = schoolOperatorMembershipRepository;
	}

	/** 교사 대시보드 — 교사 소속 학교 범위 내 데이터만 반환합니다. */
	@GetMapping("/teacher/question-sets/{questionSetId}/dashboard")
	public TeacherDashboardResponse teacherDashboard(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String questionSetId) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return dashboardService.getTeacherDashboard(teacher.schoolId(), questionSetId);
	}

	/** 자료 대시보드 — 교사 소속 학교 범위 내 데이터만 반환합니다. */
	@GetMapping("/teacher/materials/{materialId}/dashboard")
	public DocumentDashboardResponse materialDashboard(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String materialId) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return dashboardService.getDocumentDashboard(teacher.schoolId(), materialId);
	}

	/** 운영자 개요 대시보드 — 운영자 소속 학교 범위 내 데이터만 반환합니다. */
	@GetMapping("/operator/overview")
	public OperatorOverviewResponse operatorOverview(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		AuthUser operator = authService.requireRole(authorizationHeader, Role.OPERATOR);
		List<String> managedSchoolIds = schoolOperatorMembershipRepository.findByUserIdAndActiveTrue(operator.userId()).stream()
			.map(membership -> membership.getSchoolId())
			.toList();
		if (managedSchoolIds.isEmpty()) {
			throw new ForbiddenException("관리 가능한 학교가 없습니다.");
		}
		return dashboardService.getOperatorOverview(managedSchoolIds);
	}
}
