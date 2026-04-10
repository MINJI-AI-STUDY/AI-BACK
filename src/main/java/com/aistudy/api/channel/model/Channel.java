package com.aistudy.api.channel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "channels")
public class Channel {
	@Id
	private String id;

	@Column(name = "school_id", nullable = false)
	private String schoolId;

	@Column(nullable = false)
	private String name;

	@Column(length = 1000)
	private String description;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "created_by", nullable = false)
	private String createdBy;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected Channel() {}

	public Channel(String schoolId, String name, String description, int sortOrder, String createdBy) {
		this.id = UUID.randomUUID().toString();
		this.schoolId = schoolId;
		this.name = name;
		this.description = description;
		this.sortOrder = sortOrder;
		this.createdBy = createdBy;
		this.active = true;
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
	public String getName() { return name; }
	public String getDescription() { return description; }
	public int getSortOrder() { return sortOrder; }
	public boolean isActive() { return active; }

	public void update(String name, String description, int sortOrder, boolean active) {
		this.name = name;
		this.description = description;
		this.sortOrder = sortOrder;
		this.active = active;
	}
}
