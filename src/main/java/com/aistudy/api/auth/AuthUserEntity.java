package com.aistudy.api.auth;

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
@Table(name = "app_users")
public class AuthUserEntity {
	@Id
	private String id;

	@Column(name = "school_id", nullable = false)
	private String schoolId;

	@Column(name = "classroom_id")
	private String classroomId;

	@Column(name = "login_id", nullable = false, unique = true)
	private String loginId;

	@Column(nullable = false)
	private String password;

	@Column(name = "display_name", nullable = false)
	private String displayName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected AuthUserEntity() {
	}

	public AuthUserEntity(String schoolId, String classroomId, String loginId, String password, String displayName, Role role) {
		this.id = UUID.randomUUID().toString();
		this.schoolId = schoolId;
		this.classroomId = classroomId;
		this.loginId = loginId;
		this.password = password;
		this.displayName = displayName;
		this.role = role;
		this.active = true;
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public String getId() { return id; }
	public String getSchoolId() { return schoolId; }
	public String getClassroomId() { return classroomId; }
	public String getLoginId() { return loginId; }
	public String getPassword() { return password; }
	public String getDisplayName() { return displayName; }
	public Role getRole() { return role; }
	public boolean isActive() { return active; }
	public LocalDateTime getCreatedAt() { return createdAt; }

	public AuthUser toAuthUser() {
		return new AuthUser(id, schoolId, classroomId, loginId, password, displayName, role);
	}

	public void update(String schoolId, String classroomId, String displayName, Role role, boolean active) {
		this.schoolId = schoolId;
		this.classroomId = classroomId;
		this.displayName = displayName;
		this.role = role;
		this.active = active;
	}

	public void updatePassword(String password) {
		this.password = password;
	}
}
