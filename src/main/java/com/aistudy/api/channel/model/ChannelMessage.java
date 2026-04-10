package com.aistudy.api.channel.model;

import com.aistudy.api.auth.Role;
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
@Table(name = "channel_messages")
public class ChannelMessage {
	@Id
	private String id;

	@Column(name = "school_id", nullable = false)
	private String schoolId;

	@Column(name = "channel_id", nullable = false)
	private String channelId;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(name = "display_name", nullable = false)
	private String displayName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(columnDefinition = "text", nullable = false)
	private String content;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected ChannelMessage() {}

	public ChannelMessage(String schoolId, String channelId, String userId, String displayName, Role role, String content) {
		this.id = UUID.randomUUID().toString();
		this.schoolId = schoolId;
		this.channelId = channelId;
		this.userId = userId;
		this.displayName = displayName;
		this.role = role;
		this.content = content;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public String getId() { return id; }
	public String getChannelId() { return channelId; }
	public String getUserId() { return userId; }
	public String getDisplayName() { return displayName; }
	public Role getRole() { return role; }
	public String getContent() { return content; }
	public LocalDateTime getCreatedAt() { return createdAt; }
}
