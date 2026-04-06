package com.aistudy.api.submission.dto;

import com.aistudy.api.question.model.QuestionSet;
import java.time.LocalDateTime;
import java.util.List;

public record StudentQuestionSetResponse(String questionSetId, String title, LocalDateTime dueAt, List<StudentQuestionResponse> questions) {
	public static StudentQuestionSetResponse from(QuestionSet questionSet, String title) {
		return new StudentQuestionSetResponse(
			questionSet.getId(),
			title,
			questionSet.getDueAt(),
			questionSet.getQuestions().stream().filter(question -> !question.isExcluded()).map(StudentQuestionResponse::from).toList()
		);
	}
}
