package com.aistudy.api.material.model;

import java.time.LocalDateTime;

public class Material {

	private final String id;
	private final String teacherId;
	private final String title;
	private final String description;
	private final String filePath;
	private final LocalDateTime createdAt;
	private MaterialStatus status;
	private String failureReason;
	private String extractedText;

	public Material(String id, String teacherId, String title, String description, String filePath) {
		this.id = id;
		this.teacherId = teacherId;
		this.title = title;
		this.description = description;
		this.filePath = filePath;
		this.createdAt = LocalDateTime.now();
		this.status = MaterialStatus.UPLOADED;
	}

	public String getId() { return id; }
	public String getTeacherId() { return teacherId; }
	public String getTitle() { return title; }
	public String getDescription() { return description; }
	public String getFilePath() { return filePath; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public MaterialStatus getStatus() { return status; }
	public String getFailureReason() { return failureReason; }
	public String getExtractedText() { return extractedText; }

	/** 자료 상태를 처리 중으로 전환합니다. */
	public void startProcessing() {
		this.status = MaterialStatus.PROCESSING;
		this.failureReason = null;
	}

	/** 자료 상태를 완료로 전환하고 추출 텍스트를 저장합니다. */
	public void markReady(String extractedText) {
		this.status = MaterialStatus.READY;
		this.extractedText = extractedText;
		this.failureReason = null;
	}

	/** 자료 상태를 실패로 전환합니다. */
	public void markFailed(String failureReason) {
		this.status = MaterialStatus.FAILED;
		this.failureReason = failureReason;
	}
}
