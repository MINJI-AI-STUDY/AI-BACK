package com.aistudy.api.channel.service;

import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.channel.dto.ChannelMessageResponse;
import com.aistudy.api.channel.model.ChannelMessage;
import com.aistudy.api.channel.repository.ChannelMessageRepository;
import com.aistudy.api.channel.repository.ChannelRepository;
import com.aistudy.api.common.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChannelMessageService {
	private final ChannelMessageRepository channelMessageRepository;
	private final ChannelRepository channelRepository;

	public ChannelMessageService(ChannelMessageRepository channelMessageRepository, ChannelRepository channelRepository) {
		this.channelMessageRepository = channelMessageRepository;
		this.channelRepository = channelRepository;
	}

	@Transactional
	public ChannelMessageResponse send(AuthUser user, String channelId, String content) {
		channelRepository.findByIdAndSchoolId(channelId, user.schoolId()).orElseThrow(() -> new NotFoundException("채널을 찾을 수 없습니다."));
		ChannelMessage message = channelMessageRepository.save(new ChannelMessage(user.schoolId(), channelId, user.userId(), user.displayName(), user.role(), content));
		return ChannelMessageResponse.from(message);
	}

	@Transactional(readOnly = true)
	public List<ChannelMessageResponse> recent(String channelId) {
		List<ChannelMessage> messages = channelMessageRepository.findTop50ByChannelIdOrderByCreatedAtDesc(channelId);
		return messages.stream().map(ChannelMessageResponse::from).toList().reversed();
	}
}
