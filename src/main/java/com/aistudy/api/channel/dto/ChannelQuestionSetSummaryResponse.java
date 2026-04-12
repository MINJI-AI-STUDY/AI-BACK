package com.aistudy.api.channel.dto;

import com.aistudy.api.question.model.QuestionSet;
import com.aistudy.api.question.model.QuestionSetStatus;
import java.time.LocalDateTime;

public record ChannelQuestionSetSummaryResponse(
	String questionSetId,
	String materialId,
	String channelId,
	QuestionSetStatus status,
	String distributionCode,
	LocalDateTime dueAt,
	LocalDateTime createdAt,
	int questionCount
) {
	public static ChannelQuestionSetSummaryResponse from(QuestionSet questionSet) {
		return new ChannelQuestionSetSummaryResponse(
			questionSet.getId(),
			questionSet.getMaterialId(),
			questionSet.getChannelId(),
			questionSet.getStatus(),
			questionSet.getDistributionCode(),
			questionSet.getDueAt(),
			questionSet.getCreatedAt(),
			questionSet.getQuestions().size()
		);
	}
}
