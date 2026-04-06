package com.aistudy.api.dashboard.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.common.NotFoundException;
import com.aistudy.api.dashboard.dto.OperatorOverviewResponse;
import com.aistudy.api.dashboard.dto.TeacherDashboardResponse;
import com.aistudy.api.dashboard.service.DashboardService;
import com.aistudy.api.question.service.QuestionSetService;
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
	private final QuestionSetService questionSetService;

	public DashboardController(AuthService authService, DashboardService dashboardService, QuestionSetService questionSetService) {
		this.authService = authService;
		this.dashboardService = dashboardService;
		this.questionSetService = questionSetService;
	}

	/** 교사 대시보드를 반환합니다. */
	@GetMapping("/teacher/question-sets/{questionSetId}/dashboard")
	public TeacherDashboardResponse teacherDashboard(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String questionSetId) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		if (!questionSetService.getById(questionSetId).getTeacherId().equals(teacher.userId())) {
			throw new NotFoundException("대시보드를 찾을 수 없습니다.");
		}
		return dashboardService.getTeacherDashboard(questionSetId);
	}

	/** 운영자 요약 대시보드를 반환합니다. */
	@GetMapping("/operator/overview")
	public OperatorOverviewResponse operatorOverview(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		authService.requireRole(authorizationHeader, Role.OPERATOR);
		return dashboardService.getOperatorOverview();
	}
}
