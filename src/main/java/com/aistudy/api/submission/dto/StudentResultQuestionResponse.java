package com.aistudy.api.submission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.aistudy.api.submission.model.SubmissionAnswerResult;
import java.util.List;

public record StudentResultQuestionResponse(
	@JsonProperty("questionId") String questionId,
	@JsonProperty("selectedOptionIndex") int selectedOptionIndex,
	@JsonProperty("correct") boolean correct,
	@JsonProperty("explanation") String explanation,
	@JsonProperty("conceptTags") List<String> conceptTags
) {
	public static StudentResultQuestionResponse from(SubmissionAnswerResult result) {
		return new StudentResultQuestionResponse(
			result.questionId(),
			result.selectedOptionIndex(),
			result.correct(),
			result.explanation(),
			result.conceptTags()
		);
	}
}
