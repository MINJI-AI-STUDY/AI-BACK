package com.aistudy.api.question.dto;

import com.aistudy.api.question.model.Question;
import java.util.List;

public record QuestionResponse(String id, String stem, List<String> options, int correctOptionIndex, String explanation, List<String> conceptTags, boolean excluded) {
	public static QuestionResponse from(Question question) {
		return new QuestionResponse(question.getId(), question.getStem(), question.getOptions(), question.getCorrectOptionIndex(), question.getExplanation(), question.getConceptTags(), question.isExcluded());
	}
}
