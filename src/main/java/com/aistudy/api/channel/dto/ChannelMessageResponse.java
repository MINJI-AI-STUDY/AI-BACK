package com.aistudy.api.channel.dto;

import com.aistudy.api.auth.Role;
import com.aistudy.api.channel.model.ChannelMessage;
import java.time.LocalDateTime;

public record ChannelMessageResponse(String messageId, String userId, String displayName, Role role, String content, LocalDateTime createdAt) {
	public static ChannelMessageResponse from(ChannelMessage message) {
		return new ChannelMessageResponse(message.getId(), message.getUserId(), message.getDisplayName(), message.getRole(), message.getContent(), message.getCreatedAt());
	}
}
