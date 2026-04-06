package com.aistudy.api.submission.dto;

import com.aistudy.api.submission.model.Submission;
import java.util.List;

public record StudentResultResponse(int score, List<?> questionResults, List<String> explanations) {
	public static StudentResultResponse from(Submission submission) {
		return new StudentResultResponse(submission.getScore(), submission.getQuestionResults(), submission.getQuestionResults().stream().map(result -> result.explanation()).toList());
	}
}
