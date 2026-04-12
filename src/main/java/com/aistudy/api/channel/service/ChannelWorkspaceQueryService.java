package com.aistudy.api.channel.service;

import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.channel.dto.ChannelParticipantResponse;
import com.aistudy.api.channel.dto.ChannelResponse;
import com.aistudy.api.channel.dto.ChannelWorkspaceResponse;
import com.aistudy.api.material.dto.MaterialSummaryResponse;
import com.aistudy.api.material.repository.MaterialRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChannelWorkspaceQueryService {
	private final ChannelService channelService;
	private final MaterialRepository materialRepository;
	private final ChannelMessageService channelMessageService;
	private final ChannelPresenceService channelPresenceService;

	public ChannelWorkspaceQueryService(
		ChannelService channelService,
		MaterialRepository materialRepository,
		ChannelMessageService channelMessageService,
		ChannelPresenceService channelPresenceService
	) {
		this.channelService = channelService;
		this.materialRepository = materialRepository;
		this.channelMessageService = channelMessageService;
		this.channelPresenceService = channelPresenceService;
	}

	/** 채널 워크스페이스 읽기 모델 — 채널, 자료, 최근 메시지, 현재 참여자를 한 번에 조립합니다. */
	@Transactional(readOnly = true)
	public ChannelWorkspaceResponse getWorkspace(AuthUser user, String channelId) {
		var channel = channelService.get(user.schoolId(), channelId);
		List<MaterialSummaryResponse> materials = materialRepository.findByChannelIdOrderByCreatedAtDesc(channel.getId()).stream().map(MaterialSummaryResponse::from).toList();
		var recentMessages = channelMessageService.recent(channel.getId());
		List<ChannelParticipantResponse> participants = channelPresenceService.current(channel.getId());
		return new ChannelWorkspaceResponse(ChannelResponse.from(channel), materials, recentMessages, participants);
	}
}
