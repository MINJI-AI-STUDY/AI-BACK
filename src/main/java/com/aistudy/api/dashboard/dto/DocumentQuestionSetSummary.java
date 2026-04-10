package com.aistudy.api.dashboard.dto;

import com.aistudy.api.question.model.QuestionSet;
import com.aistudy.api.question.model.QuestionSetStatus;
import java.time.LocalDateTime;

public record DocumentQuestionSetSummary(
	String questionSetId,
	QuestionSetStatus status,
	String distributionCode,
	LocalDateTime dueAt,
	int questionCount,
	LocalDateTime createdAt
) {
	public static DocumentQuestionSetSummary from(QuestionSet questionSet) {
		return new DocumentQuestionSetSummary(
			questionSet.getId(),
			questionSet.getStatus(),
			questionSet.getDistributionCode(),
			questionSet.getDueAt(),
			(int) questionSet.getQuestions().stream().filter(question -> !question.isExcluded()).count(),
			questionSet.getCreatedAt()
		);
	}
}
