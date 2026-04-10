package com.aistudy.api.dashboard.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.dashboard.dto.DocumentDashboardResponse;
import com.aistudy.api.dashboard.dto.OperatorOverviewResponse;
import com.aistudy.api.dashboard.dto.TeacherDashboardResponse;
import com.aistudy.api.dashboard.service.DashboardService;
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

	public DashboardController(AuthService authService, DashboardService dashboardService) {
		this.authService = authService;
		this.dashboardService = dashboardService;
	}

	@GetMapping("/teacher/question-sets/{questionSetId}/dashboard")
	public TeacherDashboardResponse teacherDashboard(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String questionSetId) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return dashboardService.getTeacherDashboard(teacher.schoolId(), questionSetId);
	}

	@GetMapping("/teacher/materials/{materialId}/dashboard")
	public DocumentDashboardResponse materialDashboard(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String materialId) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return dashboardService.getDocumentDashboard(teacher.schoolId(), materialId);
	}

	@GetMapping("/operator/overview")
	public OperatorOverviewResponse operatorOverview(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		authService.requireRole(authorizationHeader, Role.OPERATOR);
		return dashboardService.getOperatorOverview();
	}
}
