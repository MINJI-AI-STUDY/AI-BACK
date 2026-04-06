package com.aistudy.api.submission.service;

import com.aistudy.api.common.BadRequestException;
import com.aistudy.api.common.NotFoundException;
import com.aistudy.api.material.service.MaterialService;
import com.aistudy.api.question.model.Question;
import com.aistudy.api.question.model.QuestionSet;
import com.aistudy.api.question.service.QuestionSetService;
import com.aistudy.api.submission.dto.SubmitAnswerRequest;
import com.aistudy.api.submission.dto.SubmitQuestionSetRequest;
import com.aistudy.api.submission.dto.StudentQuestionSetResponse;
import com.aistudy.api.submission.model.Submission;
import com.aistudy.api.submission.model.SubmissionAnswerResult;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SubmissionService {
	private final Map<String, Submission> submissions = new ConcurrentHashMap<>();
	private final QuestionSetService questionSetService;
	private final MaterialService materialService;

	public SubmissionService(QuestionSetService questionSetService, MaterialService materialService) {
		this.questionSetService = questionSetService;
		this.materialService = materialService;
	}

	/** 학생이 배포 코드를 통해 문제 세트에 입장할 수 있게 반환합니다. */
	public StudentQuestionSetResponse getQuestionSet(String distributionCode) {
		QuestionSet questionSet = questionSetService.getPublishedByCode(distributionCode);
		String title = materialService.getById(questionSet.getMaterialId()).getTitle();
		return StudentQuestionSetResponse.from(questionSet, title);
	}

	/** 학생 제출을 저장하고 자동 채점합니다. */
	public Submission submit(String studentId, String distributionCode, SubmitQuestionSetRequest request) {
		QuestionSet questionSet = questionSetService.getPublishedByCode(distributionCode);
		if (questionSet.getDueAt() != null && questionSet.getDueAt().isBefore(LocalDateTime.now())) {
			throw new BadRequestException("마감된 세트는 제출할 수 없습니다.");
		}
		boolean alreadySubmitted = submissions.values().stream().anyMatch(submission -> submission.getQuestionSetId().equals(questionSet.getId()) && submission.getStudentId().equals(studentId));
		if (alreadySubmitted) {
			throw new BadRequestException("이미 제출한 문제 세트입니다.");
		}

		List<Question> activeQuestions = questionSet.getQuestions().stream().filter(question -> !question.isExcluded()).toList();
		List<SubmissionAnswerResult> results = new ArrayList<>();
		int correctCount = 0;

		for (Question question : activeQuestions) {
			SubmitAnswerRequest answer = request.answers().stream().filter(candidate -> candidate.questionId().equals(question.getId())).findFirst().orElseThrow(() -> new BadRequestException("모든 문항에 답변해야 합니다."));
			boolean correct = answer.selectedOptionIndex() == question.getCorrectOptionIndex();
			if (correct) {
				correctCount++;
			}
			results.add(new SubmissionAnswerResult(question.getId(), answer.selectedOptionIndex(), correct, question.getExplanation(), question.getConceptTags()));
		}

		int score = activeQuestions.isEmpty() ? 0 : Math.round((correctCount * 100.0f) / activeQuestions.size());
		Submission submission = new Submission("submission-" + UUID.randomUUID(), questionSet.getId(), studentId, score, LocalDateTime.now(), results);
		submissions.put(submission.getId(), submission);
		return submission;
	}

	/** 제출 ID로 결과를 조회합니다. */
	public Submission getOwnedSubmission(String studentId, String submissionId) {
		Submission submission = submissions.get(submissionId);
		if (submission == null || !submission.getStudentId().equals(studentId)) {
			throw new NotFoundException("제출 결과를 찾을 수 없습니다.");
		}
		return submission;
	}
}
