package com.aistudy.api.channel.service;

import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.channel.dto.CurateTestChannelsResponse;
import com.aistudy.api.channel.model.Channel;
import com.aistudy.api.channel.repository.ChannelRepository;
import com.aistudy.api.common.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChannelService {
	private final ChannelRepository channelRepository;

	public ChannelService(ChannelRepository channelRepository) {
		this.channelRepository = channelRepository;
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

	/** 테스트용 채널을 의도한 소수 세트로 정리합니다. 유지 대상 외 채널은 비활성화합니다. */
	@Transactional
	public CurateTestChannelsResponse curateForTesting(AuthUser operator, String schoolId, List<String> keepNames) {
		List<Channel> channels = channelRepository.findBySchoolIdOrderBySortOrderAsc(schoolId);
		Map<String, Channel> byName = channels.stream().collect(Collectors.toMap(Channel::getName, Function.identity(), (left, right) -> left));
		int createdCount = 0;
		int deactivatedCount = 0;

		for (int index = 0; index < keepNames.size(); index++) {
			String name = keepNames.get(index);
			Channel existing = byName.get(name);
			if (existing == null) {
				channelRepository.save(new Channel(schoolId, name, "실사용 QA용 기본 채널", index + 1, operator.userId()));
				createdCount++;
			} else {
				existing.update(existing.getName(), existing.getDescription(), index + 1, true);
				channelRepository.save(existing);
			}
		}

		for (Channel channel : channels) {
			if (!keepNames.contains(channel.getName()) && channel.isActive()) {
				channel.update(channel.getName(), channel.getDescription(), channel.getSortOrder(), false);
				channelRepository.save(channel);
				deactivatedCount++;
			}
		}

		int keptCount = channelRepository.findBySchoolIdAndActiveTrueOrderBySortOrderAsc(schoolId).size();
		return new CurateTestChannelsResponse(keptCount, createdCount, deactivatedCount);
	}
}
