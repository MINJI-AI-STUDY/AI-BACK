package com.aistudy.api.channel.dto;

import java.util.List;

public record ChannelEventResponse(String type, List<ChannelParticipantResponse> participants, ChannelMessageResponse message) {
	public static ChannelEventResponse presence(List<ChannelParticipantResponse> participants) {
		return new ChannelEventResponse("presence", participants, null);
	}

	public static ChannelEventResponse message(ChannelMessageResponse message) {
		return new ChannelEventResponse("message", null, message);
	}
}
