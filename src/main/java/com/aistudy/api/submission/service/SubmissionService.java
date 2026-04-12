package com.aistudy.api.submission.service;

import com.aistudy.api.common.BadRequestException;
import com.aistudy.api.common.NotFoundException;
import com.aistudy.api.material.service.MaterialService;
import com.aistudy.api.question.model.Question;
import com.aistudy.api.question.model.QuestionSet;
import com.aistudy.api.question.model.QuestionSetStatus;
import com.aistudy.api.question.service.QuestionSetService;
import com.aistudy.api.question.repository.QuestionSetRepository;
import com.aistudy.api.submission.dto.StudentActiveQuestionSetResponse;
import com.aistudy.api.submission.dto.StudentQuestionSetResponse;
import com.aistudy.api.submission.dto.SubmitAnswerRequest;
import com.aistudy.api.submission.dto.SubmitQuestionSetRequest;
import com.aistudy.api.submission.model.Submission;
import com.aistudy.api.submission.model.SubmissionAnswerResult;
import com.aistudy.api.submission.repository.SubmissionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionService {
	private final SubmissionRepository submissionRepository;
	private final QuestionSetService questionSetService;
	private final QuestionSetRepository questionSetRepository;
	private final MaterialService materialService;

	public SubmissionService(SubmissionRepository submissionRepository, QuestionSetService questionSetService, QuestionSetRepository questionSetRepository, MaterialService materialService) {
		this.submissionRepository = submissionRepository;
		this.questionSetService = questionSetService;
		this.questionSetRepository = questionSetRepository;
		this.materialService = materialService;
	}

	/** 학생 채널 워크스페이스에서 현재 자료의 활성 문제 세트를 조회합니다. */
	@Transactional(readOnly = true)
	public StudentActiveQuestionSetResponse getActiveQuestionSet(String schoolId, String materialId) {
		var material = materialService.getSchoolMaterial(schoolId, materialId);
		QuestionSet questionSet = questionSetRepository
			.findFirstByMaterialIdAndSchoolIdAndStatusOrderByCreatedAtDesc(materialId, schoolId, QuestionSetStatus.PUBLISHED)
			.orElseThrow(() -> new NotFoundException("활성화된 문제 세트를 찾을 수 없습니다."));
		return StudentActiveQuestionSetResponse.from(questionSet, material.getTitle());
	}

	/** 학생 문제 세트 조회 — 배포 코드와 학교 소속을 함께 검증합니다. */
	@Transactional(readOnly = true)
	public StudentQuestionSetResponse getQuestionSet(String distributionCode, String schoolId) {
		QuestionSet questionSet = questionSetService.getPublishedByCode(distributionCode);
		if (!questionSet.getSchoolId().equals(schoolId)) {
			throw new NotFoundException("문제 세트를 찾을 수 없습니다.");
		}
		String title = materialService.getSchoolMaterial(schoolId, questionSet.getMaterialId()).getTitle();
		return StudentQuestionSetResponse.from(questionSet, title);
	}

	/** 학생 제출 처리 — 학교 소속, 중복 제출, 마감 여부를 검증한 뒤 채점 결과를 저장합니다. */
	@Transactional
	public Submission submit(String studentId, String schoolId, String distributionCode, SubmitQuestionSetRequest request) {
		QuestionSet questionSet = questionSetService.getPublishedByCode(distributionCode);
		if (!questionSet.getSchoolId().equals(schoolId)) {
			throw new NotFoundException("문제 세트를 찾을 수 없습니다.");
		}
		if (questionSet.getDueAt() != null && questionSet.getDueAt().isBefore(LocalDateTime.now())) {
			throw new BadRequestException("마감된 세트는 제출할 수 없습니다.");
		}
		if (submissionRepository.existsByQuestionSetIdAndStudentId(questionSet.getId(), studentId)) {
			throw new BadRequestException("이미 제출한 문제 세트입니다.");
		}

		List<Question> activeQuestions = questionSet.getQuestions().stream().filter(question -> !question.isExcluded()).toList();
		List<SubmissionAnswerResult> results = new ArrayList<>();
		int correctCount = 0;

		for (Question question : activeQuestions) {
			SubmitAnswerRequest answer = request.answers().stream()
				.filter(candidate -> candidate.questionId().equals(question.getId()))
				.findFirst()
				.orElseThrow(() -> new BadRequestException("모든 문항에 답변해야 합니다."));
			boolean correct = answer.selectedOptionIndex() == question.getCorrectOptionIndex();
			if (correct) {
				correctCount++;
			}
			results.add(new SubmissionAnswerResult(
				UUID.randomUUID().toString(),
				question.getId(),
				answer.selectedOptionIndex(),
				correct,
				question.getExplanation(),
				question.getConceptTags()
			));
		}

		int score = activeQuestions.isEmpty() ? 0 : Math.round((correctCount * 100.0f) / activeQuestions.size());
		Submission submission = new Submission(
			UUID.randomUUID().toString(),
			schoolId,
			questionSet.getMaterialId(),
			questionSet.getId(),
			studentId,
			score,
			LocalDateTime.now(),
			results
		);
		return submissionRepository.save(submission);
	}

	/** 제출 결과 조회 — 제출자 본인에게만 결과를 반환합니다. */
	@Transactional(readOnly = true)
	public Submission getOwnedSubmission(String studentId, String submissionId) {
		Submission submission = submissionRepository.findById(submissionId)
			.orElseThrow(() -> new NotFoundException("제출 결과를 찾을 수 없습니다."));
		if (!submission.getStudentId().equals(studentId)) {
			throw new NotFoundException("제출 결과를 찾을 수 없습니다.");
		}
		return submission;
	}

	/** 문제 세트 제출 목록 조회 — 교사 대시보드 집계를 위해 사용합니다. */
	@Transactional(readOnly = true)
	public List<Submission> getByQuestionSetId(String questionSetId) {
		return submissionRepository.findByQuestionSetId(questionSetId);
	}

	/** 문서 기준 제출 목록 조회 — 학교 범위 내 제출만 반환합니다. */
	@Transactional(readOnly = true)
	public List<Submission> getByMaterialId(String schoolId, String materialId) {
		return submissionRepository.findByMaterialIdAndSchoolId(materialId, schoolId);
	}

	/** 학교 범위 내 제출 목록을 반환합니다. */
	@Transactional(readOnly = true)
	public List<Submission> getBySchoolId(String schoolId) {
		return submissionRepository.findBySchoolId(schoolId);
	}

	/** 전체 제출 목록 조회 — 기존 전역 관리/테스트 호환을 위해 유지합니다. */
	@Transactional(readOnly = true)
	public List<Submission> getAll() {
		return submissionRepository.findAll();
	}
}
