package com.aistudy.api.submission.dto;

import com.aistudy.api.submission.model.Submission;
import com.aistudy.api.submission.model.SubmissionAnswerResult;
import java.util.List;

public record SubmissionResponse(String submissionId, int score, List<SubmissionAnswerResult> questionResults) {
	public static SubmissionResponse from(Submission submission) {
		return new SubmissionResponse(submission.getId(), submission.getScore(), submission.getQuestionResults());
	}
}
