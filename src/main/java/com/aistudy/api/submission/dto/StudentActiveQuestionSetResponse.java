package com.aistudy.api.submission.dto;

import com.aistudy.api.question.model.QuestionSet;
import java.time.LocalDateTime;

public record StudentActiveQuestionSetResponse(
	String questionSetId,
	String materialId,
	String distributionCode,
	String title,
	LocalDateTime dueAt
) {
	public static StudentActiveQuestionSetResponse from(QuestionSet questionSet, String title) {
		return new StudentActiveQuestionSetResponse(
			questionSet.getId(),
			questionSet.getMaterialId(),
			questionSet.getDistributionCode(),
			title,
			questionSet.getDueAt()
		);
	}
}
