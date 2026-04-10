package com.aistudy.api.material.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "materials")
public class Material {

	@Id
	private String id;

	@Column(name = "school_id", nullable = false)
	private String schoolId;

	@Column(name = "channel_id")
	private String channelId;

	@Column(name = "teacher_id", nullable = false)
	private String teacherId;

	@Column(name = "doc_no", nullable = false)
	private Long docNo;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "text")
	private String description;

	@Column(name = "original_file_name", nullable = false)
	private String originalFileName;

	@Column(name = "file_path", nullable = false, length = 1000)
	private String filePath;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MaterialStatus status;

	@Column(name = "failure_reason", length = 1000)
	private String failureReason;

	@Column(name = "extracted_text", columnDefinition = "text")
	private String extractedText;

	protected Material() {
	}

	public Material(String id, String schoolId, String channelId, String teacherId, Long docNo, String title, String description, String originalFileName, String filePath) {
		LocalDateTime now = LocalDateTime.now();
		this.id = id;
		this.schoolId = schoolId;
		this.channelId = channelId;
		this.teacherId = teacherId;
		this.docNo = docNo;
		this.title = title;
		this.description = description;
		this.originalFileName = originalFileName;
		this.filePath = filePath;
		this.createdAt = now;
		this.updatedAt = now;
		this.status = MaterialStatus.UPLOADED;
	}

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public String getId() { return id; }
	public String getSchoolId() { return schoolId; }
	public String getChannelId() { return channelId; }
	public String getTeacherId() { return teacherId; }
	public Long getDocNo() { return docNo; }
	public String getTitle() { return title; }
	public String getDescription() { return description; }
	public String getOriginalFileName() { return originalFileName; }
	public String getFilePath() { return filePath; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public MaterialStatus getStatus() { return status; }
	public String getFailureReason() { return failureReason; }
	public String getExtractedText() { return extractedText; }

	public void startProcessing() {
		this.status = MaterialStatus.PROCESSING;
		this.failureReason = null;
	}

	public void markReady(String extractedText) {
		this.status = MaterialStatus.READY;
		this.extractedText = extractedText;
		this.failureReason = null;
	}

	public void markFailed(String failureReason) {
		this.status = MaterialStatus.FAILED;
		this.failureReason = failureReason;
	}

	public void updateFilePath(String filePath) {
		this.filePath = filePath;
	}
}
