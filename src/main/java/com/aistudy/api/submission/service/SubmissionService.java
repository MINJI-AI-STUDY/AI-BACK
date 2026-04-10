package com.aistudy.api.submission.service;

import com.aistudy.api.common.BadRequestException;
import com.aistudy.api.common.NotFoundException;
import com.aistudy.api.material.service.MaterialService;
import com.aistudy.api.question.model.Question;
import com.aistudy.api.question.model.QuestionSet;
import com.aistudy.api.question.service.QuestionSetService;
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
	private final MaterialService materialService;

	public SubmissionService(SubmissionRepository submissionRepository, QuestionSetService questionSetService, MaterialService materialService) {
		this.submissionRepository = submissionRepository;
		this.questionSetService = questionSetService;
		this.materialService = materialService;
	}

	@Transactional(readOnly = true)
	public StudentQuestionSetResponse getQuestionSet(String distributionCode, String schoolId) {
		QuestionSet questionSet = questionSetService.getPublishedByCode(distributionCode);
		if (!questionSet.getSchoolId().equals(schoolId)) {
			throw new NotFoundException("문제 세트를 찾을 수 없습니다.");
		}
		String title = materialService.getSchoolMaterial(schoolId, questionSet.getMaterialId()).getTitle();
		return StudentQuestionSetResponse.from(questionSet, title);
	}

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

	@Transactional(readOnly = true)
	public Submission getOwnedSubmission(String studentId, String submissionId) {
		Submission submission = submissionRepository.findById(submissionId)
			.orElseThrow(() -> new NotFoundException("제출 결과를 찾을 수 없습니다."));
		if (!submission.getStudentId().equals(studentId)) {
			throw new NotFoundException("제출 결과를 찾을 수 없습니다.");
		}
		return submission;
	}

	@Transactional(readOnly = true)
	public List<Submission> getByQuestionSetId(String questionSetId) {
		return submissionRepository.findByQuestionSetId(questionSetId);
	}

	@Transactional(readOnly = true)
	public List<Submission> getByMaterialId(String schoolId, String materialId) {
		return submissionRepository.findByMaterialIdAndSchoolId(materialId, schoolId);
	}

	@Transactional(readOnly = true)
	public List<Submission> getAll() {
		return submissionRepository.findAll();
	}
}
