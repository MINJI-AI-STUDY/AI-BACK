package com.aistudy.api.qa.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.qa.dto.QALogResponse;
import com.aistudy.api.qa.dto.QaRequest;
import com.aistudy.api.qa.dto.QaResponse;
import com.aistudy.api.qa.service.QaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class QaController {
	private final AuthService authService;
	private final QaService qaService;

	public QaController(AuthService authService, QaService qaService) {
		this.authService = authService;
		this.qaService = qaService;
	}

	/** 학생 질문 — 학생 소속 학교의 자료에만 접근 가능합니다. */
	@PostMapping("/student/materials/{materialId}/qa")
	public QaResponse ask(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String materialId, @Valid @RequestBody QaRequest request) {
		AuthUser student = authService.requireRole(authorizationHeader, Role.STUDENT);
		return qaService.ask(student.userId(), student.schoolId(), materialId, request.question());
	}

	/** 학생 QA 로그 — 학생 소속 학교 범위 내 로그만 조회됩니다. */
	@GetMapping("/student/materials/{materialId}/qa-logs/me")
	public List<QALogResponse> studentLogs(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String materialId) {
		AuthUser student = authService.requireRole(authorizationHeader, Role.STUDENT);
		return qaService.getStudentLogs(student.schoolId(), materialId, student.userId()).stream().map(QALogResponse::from).toList();
	}

	/** 교사 QA 로그 — 교사 소속 학교 범위 내 로그만 조회됩니다. */
	@GetMapping("/teacher/materials/{materialId}/qa-logs")
	public List<QALogResponse> teacherLogs(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String materialId) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return qaService.getTeacherLogs(teacher.schoolId(), materialId).stream().map(QALogResponse::from).toList();
	}
}
