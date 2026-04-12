package com.aistudy.api.channel.dto;

public record CurateTestChannelsResponse(
	int keptCount,
	int createdCount,
	int deactivatedCount
) {}
