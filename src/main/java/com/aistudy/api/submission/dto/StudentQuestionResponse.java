package com.aistudy.api.submission.dto;

import com.aistudy.api.question.model.Question;
import java.util.List;

public record StudentQuestionResponse(String id, String stem, List<String> options) {
	public static StudentQuestionResponse from(Question question) {
		return new StudentQuestionResponse(question.getId(), question.getStem(), question.getOptions());
	}
}
