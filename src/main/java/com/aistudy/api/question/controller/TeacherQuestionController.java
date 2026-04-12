package com.aistudy.api.question.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.question.dto.GenerateQuestionSetRequest;
import com.aistudy.api.question.dto.PublishQuestionSetRequest;
import com.aistudy.api.question.dto.QuestionSetResponse;
import com.aistudy.api.question.dto.UpdateQuestionRequest;
import com.aistudy.api.question.service.QuestionSetService;
import jakarta.validation.Valid;
import java.util.List;
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

	@PostMapping("/materials/{materialId}/question-sets/generate")
	public QuestionSetResponse generate(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String materialId, @Valid @RequestBody GenerateQuestionSetRequest request) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return QuestionSetResponse.from(questionSetService.generate(teacher.userId(), teacher.schoolId(), materialId, request));
	}

	@PostMapping("/channels/{channelId}/question-sets/generate")
	public QuestionSetResponse generateInChannel(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId, @Valid @RequestBody GenerateQuestionSetRequest request) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return QuestionSetResponse.from(questionSetService.generateInChannel(teacher.userId(), teacher.schoolId(), channelId, request));
	}

	@GetMapping("/materials/{materialId}/question-sets")
	public List<QuestionSetResponse> listByMaterial(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String materialId) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return questionSetService.getByMaterial(teacher.schoolId(), materialId).stream().map(QuestionSetResponse::from).toList();
	}

	@GetMapping("/channels/{channelId}/question-sets")
	public List<QuestionSetResponse> listByChannel(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return questionSetService.getByChannel(teacher.schoolId(), channelId).stream().map(QuestionSetResponse::from).toList();
	}

	@GetMapping("/question-sets/{questionSetId}")
	public QuestionSetResponse getQuestionSet(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String questionSetId) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return QuestionSetResponse.from(questionSetService.getSchoolScopedQuestionSet(teacher.schoolId(), questionSetId));
	}

	@PatchMapping("/question-sets/{questionSetId}/questions/{questionId}")
	public QuestionSetResponse updateQuestion(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String questionSetId, @PathVariable String questionId, @Valid @RequestBody UpdateQuestionRequest request) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return QuestionSetResponse.from(questionSetService.updateQuestion(teacher.userId(), teacher.schoolId(), questionSetId, questionId, request));
	}

	@PostMapping("/question-sets/{questionSetId}/publish")
	public QuestionSetResponse publish(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String questionSetId, @RequestBody(required = false) PublishQuestionSetRequest request) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return QuestionSetResponse.from(questionSetService.publish(teacher.userId(), teacher.schoolId(), questionSetId, request == null ? null : request.dueAt()));
	}
}
