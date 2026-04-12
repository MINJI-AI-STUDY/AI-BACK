package com.aistudy.api.channel.dto;

import com.aistudy.api.material.dto.MaterialSummaryResponse;
import java.util.List;

public record ChannelWorkspaceResponse(
	ChannelResponse channel,
	List<MaterialSummaryResponse> materials,
	List<ChannelQuestionSetSummaryResponse> questionSets,
	List<ChannelMessageResponse> recentMessages,
	List<ChannelParticipantResponse> participants
) {}
