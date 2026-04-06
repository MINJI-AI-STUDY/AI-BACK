package com.aistudy.api.qa.model;

import java.time.LocalDateTime;

public class QALog {
	private final String id;
	private final String materialId;
	private final String studentId;
	private final String question;
	private final String answer;
	private final boolean grounded;
	private final String status;
	private final LocalDateTime createdAt;

	public QALog(String id, String materialId, String studentId, String question, String answer, boolean grounded, String status, LocalDateTime createdAt) {
		this.id = id;
		this.materialId = materialId;
		this.studentId = studentId;
		this.question = question;
		this.answer = answer;
		this.grounded = grounded;
		this.status = status;
		this.createdAt = createdAt;
	}

	public String getId() { return id; }
	public String getMaterialId() { return materialId; }
	public String getStudentId() { return studentId; }
	public String getQuestion() { return question; }
	public String getAnswer() { return answer; }
	public boolean isGrounded() { return grounded; }
	public String getStatus() { return status; }
	public LocalDateTime getCreatedAt() { return createdAt; }
}
