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

	/** 학생 PIN 인증용 해시 (교사/운영자는 null) */
	@Column(name = "pin")
	private String pin;

	/** 학교 범위 내 학생 고유 코드 (교사/운영자는 null) */
	@Column(name = "student_code")
	private String studentCode;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected AuthUserEntity() {
	}

	public AuthUserEntity(String schoolId, String classroomId, String loginId, String password, String displayName, Role role) {
		this(schoolId, classroomId, loginId, password, displayName, role, null, null);
	}

	/** 학생 PIN + studentCode 포함 생성자 — 교사/운영자는 pin=null, studentCode=null로 위 생성자 사용 */
	public AuthUserEntity(String schoolId, String classroomId, String loginId, String password, String displayName, Role role, String pin, String studentCode) {
		this.id = UUID.randomUUID().toString();
		this.schoolId = schoolId;
		this.classroomId = classroomId;
		this.loginId = loginId;
		this.password = password;
		this.displayName = displayName;
		this.role = role;
		this.pin = pin;
		this.studentCode = studentCode;
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
	public String getPin() { return pin; }
	public String getStudentCode() { return studentCode; }
	public LocalDateTime getCreatedAt() { return createdAt; }

	public AuthUser toAuthUser() {
		return new AuthUser(id, schoolId, classroomId, loginId, password, displayName, role, studentCode);
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
