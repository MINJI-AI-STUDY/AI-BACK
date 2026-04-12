package com.aistudy.api.signup.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.signup.dto.CreateStudentSignupRequest;
import com.aistudy.api.signup.dto.CreateTeacherSignupRequest;
import com.aistudy.api.signup.dto.ReviewSignupRequest;
import com.aistudy.api.signup.dto.SchoolMasterResponse;
import com.aistudy.api.signup.dto.SignupRequestResponse;
import com.aistudy.api.signup.service.SignupService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signup")
public class SignupController {
	private final SignupService signupService;
	private final AuthService authService;

	public SignupController(SignupService signupService, AuthService authService) {
		this.signupService = signupService;
		this.authService = authService;
	}

	/** 학교 검색 — 가입 화면에서 선택 가능한 학교 마스터 목록을 반환합니다. */
	@GetMapping("/schools")
	public List<SchoolMasterResponse> searchSchools(@RequestParam(defaultValue = "") String keyword) {
		return signupService.searchSchools(keyword).stream().map(SchoolMasterResponse::from).toList();
	}

	/** 교직원 가입 요청 생성 — 학교 활성 여부를 검증한 뒤 승인 대기 상태로 저장합니다. */
	@PostMapping("/teacher")
	public SignupRequestResponse requestTeacherSignup(@Valid @RequestBody CreateTeacherSignupRequest request) {
		return SignupRequestResponse.from(signupService.requestTeacherSignup(request));
	}

	/** 학생 가입 요청 생성 — 학교 활성 여부와 학급-학교 소속을 검증한 뒤 승인 대기 상태로 저장합니다. */
	@PostMapping("/student")
	public SignupRequestResponse requestStudentSignup(@Valid @RequestBody CreateStudentSignupRequest request) {
		return SignupRequestResponse.from(signupService.requestStudentSignup(request));
	}

	/** 가입 요청 목록 조회 — 운영자는 자신이 관리하는 학교의 대기 중인 요청만 볼 수 있습니다. */
	@GetMapping("/requests/pending")
	public List<SignupRequestResponse> pending(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @RequestParam String schoolId) {
		AuthUser reviewer = authService.getCurrentUser(authorizationHeader);
		return signupService.pendingRequests(reviewer, schoolId).stream().map(SignupRequestResponse::from).toList();
	}

	/** 가입 요청 승인/반려 — 운영자는 자신이 관리하는 학교의 요청만 처리할 수 있습니다. */
	@PatchMapping("/requests/{signupRequestId}")
	public SignupRequestResponse review(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String signupRequestId, @Valid @RequestBody ReviewSignupRequest request) {
		AuthUser reviewer = authService.getCurrentUser(authorizationHeader);
		return SignupRequestResponse.from(signupService.review(reviewer, signupRequestId, request.approve(), request.normalizedReason(), null));
	}
}
