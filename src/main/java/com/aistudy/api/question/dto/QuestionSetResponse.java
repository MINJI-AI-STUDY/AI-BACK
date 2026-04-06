package com.aistudy.api.question.dto;

import com.aistudy.api.question.model.QuestionSet;
import com.aistudy.api.question.model.QuestionSetStatus;
import java.time.LocalDateTime;
import java.util.List;

public record QuestionSetResponse(String questionSetId, QuestionSetStatus status, String materialId, String distributionCode, String distributionLink, LocalDateTime dueAt, List<QuestionResponse> questions) {
	public static QuestionSetResponse from(QuestionSet questionSet) {
		return new QuestionSetResponse(
			questionSet.getId(),
			questionSet.getStatus(),
			questionSet.getMaterialId(),
			questionSet.getDistributionCode(),
			questionSet.getDistributionLink(),
			questionSet.getDueAt(),
			questionSet.getQuestions().stream().map(QuestionResponse::from).toList()
		);
	}
}
