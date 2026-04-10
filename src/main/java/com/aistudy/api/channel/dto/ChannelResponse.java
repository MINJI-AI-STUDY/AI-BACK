package com.aistudy.api.channel.dto;

import com.aistudy.api.channel.model.Channel;

public record ChannelResponse(String channelId, String schoolId, String name, String description, int sortOrder, boolean active) {
	public static ChannelResponse from(Channel channel) {
		return new ChannelResponse(channel.getId(), channel.getSchoolId(), channel.getName(), channel.getDescription(), channel.getSortOrder(), channel.isActive());
	}
}
