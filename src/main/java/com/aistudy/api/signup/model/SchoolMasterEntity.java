package com.aistudy.api.signup.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "school_master")
public class SchoolMasterEntity {
	@Id
	private String id;

	@Column(name = "official_school_code", nullable = false)
	private String officialSchoolCode;

	@Column(nullable = false)
	private String name;

	@Column(name = "school_level", nullable = false)
	private String schoolLevel;

	@Column
	private String address;

	@Column
	private String region;

	@Column(name = "email_domain")
	private String emailDomain;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected SchoolMasterEntity() {}

	public String getId() { return id; }
	public String getOfficialSchoolCode() { return officialSchoolCode; }
	public String getName() { return name; }
	public String getSchoolLevel() { return schoolLevel; }
	public String getAddress() { return address; }
	public String getRegion() { return region; }
	public String getEmailDomain() { return emailDomain; }
	public boolean isActive() { return active; }
}
