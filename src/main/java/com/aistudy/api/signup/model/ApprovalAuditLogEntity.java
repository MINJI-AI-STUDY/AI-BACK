package com.aistudy.api.signup.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "approval_audit_logs")
public class ApprovalAuditLogEntity {
	@Id
	private String id;

	@Column(name = "school_id", nullable = false)
	private String schoolId;

	@Column(name = "signup_request_id", nullable = false)
	private String signupRequestId;

	@Column(name = "reviewer_user_id", nullable = false)
	private String reviewerUserId;

	@Column(nullable = false)
	private String action;

	@Column
	private String note;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected ApprovalAuditLogEntity() {}

	public ApprovalAuditLogEntity(String schoolId, String signupRequestId, String reviewerUserId, String action, String note) {
		this.id = UUID.randomUUID().toString();
		this.schoolId = schoolId;
		this.signupRequestId = signupRequestId;
		this.reviewerUserId = reviewerUserId;
		this.action = action;
		this.note = note;
	}

	@PrePersist
	void onCreate() { this.createdAt = LocalDateTime.now(); }
}
