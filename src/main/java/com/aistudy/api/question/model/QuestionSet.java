package com.aistudy.api.question.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuestionSet {
	private final String id;
	private final String materialId;
	private final String teacherId;
	private final Difficulty difficulty;
	private final List<Question> questions;
	private QuestionSetStatus status;
	private String distributionCode;
	private String distributionLink;
	private LocalDateTime dueAt;

	public QuestionSet(String id, String materialId, String teacherId, Difficulty difficulty, List<Question> questions) {
		this.id = id;
		this.materialId = materialId;
		this.teacherId = teacherId;
		this.difficulty = difficulty;
		this.questions = new ArrayList<>(questions);
		this.status = QuestionSetStatus.REVIEW_REQUIRED;
	}

	public String getId() { return id; }
	public String getMaterialId() { return materialId; }
	public String getTeacherId() { return teacherId; }
	public Difficulty getDifficulty() { return difficulty; }
	public List<Question> getQuestions() { return questions; }
	public QuestionSetStatus getStatus() { return status; }
	public String getDistributionCode() { return distributionCode; }
	public String getDistributionLink() { return distributionLink; }
	public LocalDateTime getDueAt() { return dueAt; }

	/** 배포 상태로 전환하고 코드와 링크를 발급합니다. */
	public void publish(String distributionCode, String distributionLink, LocalDateTime dueAt) {
		this.status = QuestionSetStatus.PUBLISHED;
		this.distributionCode = distributionCode;
		this.distributionLink = distributionLink;
		this.dueAt = dueAt;
	}
}
