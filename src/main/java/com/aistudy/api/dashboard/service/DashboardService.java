package com.aistudy.api.dashboard.service;

import com.aistudy.api.dashboard.dto.OperatorOverviewResponse;
import com.aistudy.api.dashboard.dto.QuestionAccuracy;
import com.aistudy.api.dashboard.dto.TeacherDashboardResponse;
import com.aistudy.api.dashboard.dto.TeacherStudentScore;
import com.aistudy.api.dashboard.dto.WeakConceptTag;
import com.aistudy.api.question.model.Question;
import com.aistudy.api.question.model.QuestionSet;
import com.aistudy.api.question.service.QuestionSetService;
import com.aistudy.api.submission.model.Submission;
import com.aistudy.api.submission.model.SubmissionAnswerResult;
import com.aistudy.api.submission.service.SubmissionService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
	private final QuestionSetService questionSetService;
	private final SubmissionService submissionService;

	public DashboardService(QuestionSetService questionSetService, SubmissionService submissionService) {
		this.questionSetService = questionSetService;
		this.submissionService = submissionService;
	}

	/** 교사용 대시보드 집계 결과를 계산합니다. */
	public TeacherDashboardResponse getTeacherDashboard(String questionSetId) {
		QuestionSet questionSet = questionSetService.getById(questionSetId);
		List<Submission> submissions = submissionService.getByQuestionSetId(questionSetId);
		List<TeacherStudentScore> studentScores = submissions.stream().map(submission -> new TeacherStudentScore(submission.getStudentId(), submission.getScore())).toList();

		Map<String, Double> accuracyMap = questionSet.getQuestions().stream()
			.filter(question -> !question.isExcluded())
			.collect(Collectors.toMap(Question::getId, question -> calculateAccuracy(question.getId(), submissions)));

		List<QuestionAccuracy> questionAccuracy = accuracyMap.entrySet().stream().map(entry -> new QuestionAccuracy(entry.getKey(), entry.getValue())).toList();

		Map<String, Long> weakConceptCounts = submissions.stream()
			.flatMap(submission -> submission.getQuestionResults().stream())
			.filter(result -> !result.correct())
			.flatMap(result -> result.conceptTags().stream())
			.collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

		List<WeakConceptTag> weakConceptTags = weakConceptCounts.entrySet().stream()
			.sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
			.map(entry -> new WeakConceptTag(entry.getKey(), entry.getValue()))
			.toList();

		return new TeacherDashboardResponse(studentScores, questionAccuracy, weakConceptTags);
	}

	/** 운영자용 전체 요약 지표를 계산합니다. */
	public OperatorOverviewResponse getOperatorOverview() {
		List<Submission> submissions = submissionService.getAll();
		List<QuestionSet> questionSets = questionSetService.getAll();
		double averageScore = submissions.stream().mapToInt(Submission::getScore).average().orElse(0);
		double participationRate = questionSets.isEmpty() ? 0 : Math.min(100.0, (submissions.size() * 100.0) / questionSets.size());
		double completionRate = participationRate;
		return new OperatorOverviewResponse(averageScore, participationRate, completionRate);
	}

	private double calculateAccuracy(String questionId, List<Submission> submissions) {
		List<SubmissionAnswerResult> answers = submissions.stream()
			.flatMap(submission -> submission.getQuestionResults().stream())
			.filter(result -> result.questionId().equals(questionId))
			.toList();
		if (answers.isEmpty()) {
			return 0;
		}
		long correctCount = answers.stream().filter(SubmissionAnswerResult::correct).count();
		return Math.round((correctCount * 10000.0) / answers.size()) / 100.0;
	}
}
