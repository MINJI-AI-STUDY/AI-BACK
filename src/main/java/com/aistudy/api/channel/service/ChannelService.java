package com.aistudy.api.channel.service;

import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.channel.dto.ChannelMessageResponse;
import com.aistudy.api.channel.dto.ChannelParticipantResponse;
import com.aistudy.api.channel.dto.ChannelWorkspaceResponse;
import com.aistudy.api.channel.model.Channel;
import com.aistudy.api.channel.repository.ChannelRepository;
import com.aistudy.api.common.NotFoundException;
import com.aistudy.api.material.dto.MaterialSummaryResponse;
import com.aistudy.api.material.repository.MaterialRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChannelService {
	private final ChannelRepository channelRepository;
	private final MaterialRepository materialRepository;
	private final ChannelMessageService channelMessageService;
	private final ChannelPresenceService channelPresenceService;

	public ChannelService(ChannelRepository channelRepository, MaterialRepository materialRepository, ChannelMessageService channelMessageService, ChannelPresenceService channelPresenceService) {
		this.channelRepository = channelRepository;
		this.materialRepository = materialRepository;
		this.channelMessageService = channelMessageService;
		this.channelPresenceService = channelPresenceService;
	}

	/** 학교 범위 내 채널 목록 조회 */
	@Transactional(readOnly = true)
	public List<Channel> list(String schoolId) {
		return channelRepository.findBySchoolIdAndActiveTrueOrderBySortOrderAsc(schoolId);
	}

	@Transactional(readOnly = true)
	public Channel defaultChannel(String schoolId) {
		return list(schoolId).stream().findFirst().orElseThrow(() -> new NotFoundException("기본 채널을 찾을 수 없습니다."));
	}

	/** 학교 범위 내 채널 조회 — schoolId 불일치 시 404 */
	@Transactional(readOnly = true)
	public Channel get(String schoolId, String channelId) {
		return channelRepository.findByIdAndSchoolId(channelId, schoolId)
			.orElseThrow(() -> new NotFoundException("채널을 찾을 수 없습니다."));
	}

	/** 채널 생성 — 교사 소속 학교에 귀속 */
	@Transactional
	public Channel create(AuthUser teacher, String name, String description, int sortOrder) {
		return channelRepository.save(new Channel(teacher.schoolId(), name, description, sortOrder, teacher.userId()));
	}

	/** 채널 수정 — 교사 소속 학교 범위 내 채널만 수정 가능 */
	@Transactional
	public Channel update(AuthUser teacher, String channelId, String name, String description, int sortOrder, boolean active) {
		Channel channel = get(teacher.schoolId(), channelId);
		channel.update(name, description, sortOrder, active);
		return channelRepository.save(channel);
	}

	/** 채널 워크스페이스 — 사용자 소속 학교 범위 내 채널만 접근 가능 */
	@Transactional(readOnly = true)
	public ChannelWorkspaceResponse workspace(AuthUser user, String channelId) {
		Channel channel = get(user.schoolId(), channelId);
		List<MaterialSummaryResponse> materials = materialRepository.findByChannelIdOrderByCreatedAtDesc(channel.getId()).stream().map(MaterialSummaryResponse::from).toList();
		List<ChannelMessageResponse> recentMessages = channelMessageService.recent(channel.getId());
		List<ChannelParticipantResponse> participants = channelPresenceService.current(channel.getId());
		return new ChannelWorkspaceResponse(com.aistudy.api.channel.dto.ChannelResponse.from(channel), materials, recentMessages, participants);
	}
}
