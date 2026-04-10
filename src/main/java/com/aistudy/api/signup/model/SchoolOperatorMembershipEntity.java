package com.aistudy.api.signup.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "school_operator_memberships")
public class SchoolOperatorMembershipEntity {
	@Id
	private String id;

	@Column(name = "school_id", nullable = false)
	private String schoolId;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected SchoolOperatorMembershipEntity() {}

	public SchoolOperatorMembershipEntity(String schoolId, String userId) {
		this.id = UUID.randomUUID().toString();
		this.schoolId = schoolId;
		this.userId = userId;
		this.active = true;
	}

	@PrePersist
	void onCreate() { this.createdAt = LocalDateTime.now(); }

	public String getSchoolId() { return schoolId; }
	public String getUserId() { return userId; }
	public boolean isActive() { return active; }
}
