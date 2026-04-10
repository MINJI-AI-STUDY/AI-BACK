package com.aistudy.api.signup.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "signup_requests")
public class SignupRequestEntity {
	@Id
	private String id;

	@Column(name = "school_id", nullable = false)
	private String schoolId;

	@Column(name = "classroom_id")
	private String classroomId;

	@Column(name = "requester_name", nullable = false)
	private String requesterName;

	@Column(name = "login_id")
	private String loginId;

	@Column(name = "password_hash")
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SignupRole role;

	@Column(name = "school_email")
	private String schoolEmail;

	@Column(name = "student_real_name")
	private String studentRealName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SignupStatus status;

	@Column(name = "consent_terms", nullable = false)
	private boolean consentTerms;

	@Column(name = "consent_privacy", nullable = false)
	private boolean consentPrivacy;

	@Column(name = "consent_student_notice", nullable = false)
	private boolean consentStudentNotice;

	@Column(name = "rejection_reason")
	private String rejectionReason;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "reviewed_at")
	private LocalDateTime reviewedAt;

	@Column(name = "reviewed_by")
	private String reviewedBy;

	@Column(name = "provisioned_login_id")
	private String provisionedLoginId;

	@Column(name = "provisioned_temp_password")
	private String provisionedTempPassword;

	protected SignupRequestEntity() {}

	public SignupRequestEntity(String schoolId, String classroomId, String requesterName, String loginId, String passwordHash, SignupRole role, String schoolEmail, String studentRealName, boolean consentTerms, boolean consentPrivacy, boolean consentStudentNotice) {
		this.id = UUID.randomUUID().toString();
		this.schoolId = schoolId;
		this.classroomId = classroomId;
		this.requesterName = requesterName;
		this.loginId = loginId;
		this.passwordHash = passwordHash;
		this.role = role;
		this.schoolEmail = schoolEmail;
		this.studentRealName = studentRealName;
		this.status = SignupStatus.PENDING;
		this.consentTerms = consentTerms;
		this.consentPrivacy = consentPrivacy;
		this.consentStudentNotice = consentStudentNotice;
	}

	@PrePersist
	void onCreate() { this.createdAt = LocalDateTime.now(); }

	public String getId() { return id; }
	public String getSchoolId() { return schoolId; }
	public String getClassroomId() { return classroomId; }
	public String getRequesterName() { return requesterName; }
	public String getLoginId() { return loginId; }
	public String getPasswordHash() { return passwordHash; }
	public SignupRole getRole() { return role; }
	public String getSchoolEmail() { return schoolEmail; }
	public String getStudentRealName() { return studentRealName; }
	public SignupStatus getStatus() { return status; }
	public boolean isConsentTerms() { return consentTerms; }
	public boolean isConsentPrivacy() { return consentPrivacy; }
	public boolean isConsentStudentNotice() { return consentStudentNotice; }
	public String getRejectionReason() { return rejectionReason; }
	public String getProvisionedLoginId() { return provisionedLoginId; }
	public String getProvisionedTempPassword() { return provisionedTempPassword; }

	public void approve(String reviewerUserId, String provisionedLoginId, String provisionedTempPassword) {
		this.status = SignupStatus.APPROVED;
		this.reviewedBy = reviewerUserId;
		this.reviewedAt = LocalDateTime.now();
		this.rejectionReason = null;
		this.provisionedLoginId = provisionedLoginId;
		this.provisionedTempPassword = provisionedTempPassword;
	}

	public void reject(String reviewerUserId, String rejectionReason) {
		this.status = SignupStatus.REJECTED;
		this.reviewedBy = reviewerUserId;
		this.reviewedAt = LocalDateTime.now();
		this.rejectionReason = rejectionReason;
	}
}
