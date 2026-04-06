package com.aistudy.api.question.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.common.NotFoundException;
import com.aistudy.api.question.dto.GenerateQuestionSetRequest;
import com.aistudy.api.question.dto.PublishQuestionSetRequest;
import com.aistudy.api.question.dto.QuestionSetResponse;
import com.aistudy.api.question.dto.UpdateQuestionRequest;
import com.aistudy.api.question.model.QuestionSet;
import com.aistudy.api.question.service.QuestionSetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher")
public class TeacherQuestionController {
	private final AuthService authService;
	private final QuestionSetService questionSetService;

	public TeacherQuestionController(AuthService authService, QuestionSetService questionSetService) {
		this.authService = authService;
		this.questionSetService = questionSetService;
	}

	/** 자료 기준으로 문제 세트를 생성합니다. */
	@PostMapping("/materials/{materialId}/question-sets/generate")
	public QuestionSetResponse generate(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String materialId, @Valid @RequestBody GenerateQuestionSetRequest request) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return QuestionSetResponse.from(questionSetService.generate(teacher.userId(), materialId, request));
	}

	/** 교사가 자신의 문제 세트를 다시 조회합니다. */
	@GetMapping("/question-sets/{questionSetId}")
	public QuestionSetResponse getQuestionSet(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String questionSetId) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		QuestionSet questionSet = questionSetService.getById(questionSetId);
		if (!questionSet.getTeacherId().equals(teacher.userId())) {
			throw new NotFoundException("문제 세트를 찾을 수 없습니다.");
		}
		return QuestionSetResponse.from(questionSet);
	}

	/** 교사 검토로 문항을 수정합니다. */
	@PatchMapping("/question-sets/{questionSetId}/questions/{questionId}")
	public QuestionSetResponse updateQuestion(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String questionSetId, @PathVariable String questionId, @Valid @RequestBody UpdateQuestionRequest request) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return QuestionSetResponse.from(questionSetService.updateQuestion(teacher.userId(), questionSetId, questionId, request));
	}

	/** 검토가 끝난 문제 세트를 배포합니다. */
	@PostMapping("/question-sets/{questionSetId}/publish")
	public QuestionSetResponse publish(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String questionSetId, @RequestBody(required = false) PublishQuestionSetRequest request) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return QuestionSetResponse.from(questionSetService.publish(teacher.userId(), questionSetId, request == null ? null : request.dueAt()));
	}
}
