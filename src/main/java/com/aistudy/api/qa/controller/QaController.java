package com.aistudy.api.qa.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.qa.dto.QaRequest;
import com.aistudy.api.qa.dto.QaResponse;
import com.aistudy.api.qa.service.QaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/materials")
public class QaController {
	private final AuthService authService;
	private final QaService qaService;

	public QaController(AuthService authService, QaService qaService) {
		this.authService = authService;
		this.qaService = qaService;
	}

	/** 자료 기반 질문을 처리합니다. */
	@PostMapping("/{materialId}/qa")
	public QaResponse ask(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String materialId, @Valid @RequestBody QaRequest request) {
		AuthUser student = authService.requireRole(authorizationHeader, Role.STUDENT);
		return qaService.ask(student.userId(), materialId, request.question());
	}
}
