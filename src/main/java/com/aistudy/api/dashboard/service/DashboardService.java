package com.aistudy.api.dashboard.service;

import com.aistudy.api.dashboard.dto.DocumentDashboardResponse;
import com.aistudy.api.dashboard.dto.DocumentQuestionSetSummary;
import com.aistudy.api.dashboard.dto.OperatorOverviewResponse;
import com.aistudy.api.dashboard.dto.QuestionAccuracy;
import com.aistudy.api.dashboard.dto.TeacherDashboardResponse;
import com.aistudy.api.dashboard.dto.TeacherStudentScore;
import com.aistudy.api.dashboard.dto.WeakConceptTag;
import com.aistudy.api.material.dto.MaterialSummaryResponse;
import com.aistudy.api.material.model.Material;
import com.aistudy.api.material.service.MaterialService;
import com.aistudy.api.qa.dto.QALogResponse;
import com.aistudy.api.qa.service.QaService;
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
	private final MaterialService materialService;
	private final QuestionSetService questionSetService;
	private final SubmissionService submissionService;
	private final QaService qaService;

	public DashboardService(MaterialService materialService, QuestionSetService questionSetService, SubmissionService submissionService, QaService qaService) {
		this.materialService = materialService;
		this.questionSetService = questionSetService;
		this.submissionService = submissionService;
		this.qaService = qaService;
	}

	/** 교사 문제 세트 대시보드 집계 — 교사 소속 학교 범위의 제출/정답률/취약 개념을 계산합니다. */
	public TeacherDashboardResponse getTeacherDashboard(String schoolId, String questionSetId) {
		QuestionSet questionSet = questionSetService.getSchoolScopedQuestionSet(schoolId, questionSetId);
		List<Submission> submissions = submissionService.getByQuestionSetId(questionSetId);
		List<TeacherStudentScore> studentScores = submissions.stream()
			.map(submission -> new TeacherStudentScore(submission.getStudentId(), submission.getScore()))
			.toList();

		Map<String, Double> accuracyMap = questionSet.getQuestions().stream()
			.filter(question -> !question.isExcluded())
			.collect(Collectors.toMap(Question::getId, question -> calculateAccuracy(question.getId(), submissions)));

		List<QuestionAccuracy> questionAccuracy = accuracyMap.entrySet().stream()
			.map(entry -> new QuestionAccuracy(entry.getKey(), entry.getValue()))
			.toList();

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

	/** 자료 대시보드 집계 — 학교 범위의 문제 세트, 제출, QA 로그를 문서 기준으로 요약합니다. */
	public DocumentDashboardResponse getDocumentDashboard(String schoolId, String materialId) {
		Material material = materialService.getSchoolMaterial(schoolId, materialId);
		List<QuestionSet> questionSets = questionSetService.getByMaterial(schoolId, materialId);
		List<Submission> submissions = submissionService.getByMaterialId(schoolId, materialId);
		List<QALogResponse> recentQaLogs = qaService.getTeacherLogs(schoolId, materialId).stream()
			.limit(10)
			.map(QALogResponse::from)
			.toList();

		long questionCount = questionSets.stream()
			.flatMap(questionSet -> questionSet.getQuestions().stream())
			.filter(question -> !question.isExcluded())
			.count();
		long participantCount = submissions.stream().map(Submission::getStudentId).distinct().count();
		double averageScore = submissions.stream().mapToInt(Submission::getScore).average().orElse(0);

		return new DocumentDashboardResponse(
			MaterialSummaryResponse.from(material),
			questionSets.size(),
			questionCount,
			submissions.size(),
			participantCount,
			Math.round(averageScore * 100.0) / 100.0,
			qaService.countByMaterial(schoolId, materialId),
			questionSets.stream().map(DocumentQuestionSetSummary::from).toList(),
			recentQaLogs
		);
	}

	/** 운영자 개요 — 학교 범위 내 데이터만 집계합니다. */
	public OperatorOverviewResponse getOperatorOverview(String schoolId) {
		List<Submission> submissions = submissionService.getBySchoolId(schoolId);
		List<QuestionSet> questionSets = questionSetService.getBySchoolId(schoolId);
		double averageScore = submissions.stream().mapToInt(Submission::getScore).average().orElse(0);
		long participatedSetCount = submissions.stream().map(Submission::getQuestionSetId).distinct().count();
		long completedSetCount = submissions.stream().filter(submission -> submission.getScore() >= 0).map(Submission::getQuestionSetId).distinct().count();
		double participationRate = questionSets.isEmpty() ? 0 : Math.min(100.0, (participatedSetCount * 100.0) / questionSets.size());
		double completionRate = questionSets.isEmpty() ? 0 : Math.min(100.0, (completedSetCount * 100.0) / questionSets.size());
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
