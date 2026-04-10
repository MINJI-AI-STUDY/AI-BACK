package com.aistudy.api.submission.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.submission.dto.StudentQuestionSetResponse;
import com.aistudy.api.submission.dto.StudentResultResponse;
import com.aistudy.api.submission.dto.SubmissionResponse;
import com.aistudy.api.submission.dto.SubmitQuestionSetRequest;
import com.aistudy.api.submission.model.Submission;
import com.aistudy.api.submission.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentSubmissionController {
	private final AuthService authService;
	private final SubmissionService submissionService;

	public StudentSubmissionController(AuthService authService, SubmissionService submissionService) {
		this.authService = authService;
		this.submissionService = submissionService;
	}

	@GetMapping("/question-sets/{distributionCode}")
	public StudentQuestionSetResponse getQuestionSet(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String distributionCode) {
		AuthUser student = authService.requireRole(authorizationHeader, Role.STUDENT);
		return submissionService.getQuestionSet(distributionCode, student.schoolId());
	}

	@PostMapping("/question-sets/{distributionCode}/submissions")
	public SubmissionResponse submit(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String distributionCode, @Valid @RequestBody SubmitQuestionSetRequest request) {
		AuthUser student = authService.requireRole(authorizationHeader, Role.STUDENT);
		Submission submission = submissionService.submit(student.userId(), student.schoolId(), distributionCode, request);
		return SubmissionResponse.from(submission);
	}

	@GetMapping("/submissions/{submissionId}/result")
	public StudentResultResponse result(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String submissionId) {
		AuthUser student = authService.requireRole(authorizationHeader, Role.STUDENT);
		return StudentResultResponse.from(submissionService.getOwnedSubmission(student.userId(), submissionId));
	}
}
