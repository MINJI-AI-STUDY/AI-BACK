package com.aistudy.api.submission.model;

import java.time.LocalDateTime;
import java.util.List;

public class Submission {
	private final String id;
	private final String questionSetId;
	private final String studentId;
	private final int score;
	private final LocalDateTime submittedAt;
	private final List<SubmissionAnswerResult> questionResults;

	public Submission(String id, String questionSetId, String studentId, int score, LocalDateTime submittedAt, List<SubmissionAnswerResult> questionResults) {
		this.id = id;
		this.questionSetId = questionSetId;
		this.studentId = studentId;
		this.score = score;
		this.submittedAt = submittedAt;
		this.questionResults = questionResults;
	}

	public String getId() { return id; }
	public String getQuestionSetId() { return questionSetId; }
	public String getStudentId() { return studentId; }
	public int getScore() { return score; }
	public LocalDateTime getSubmittedAt() { return submittedAt; }
	public List<SubmissionAnswerResult> getQuestionResults() { return questionResults; }
}
