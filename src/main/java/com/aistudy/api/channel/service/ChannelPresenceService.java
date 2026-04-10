package com.aistudy.api.channel.service;

import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.channel.dto.ChannelParticipantResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ChannelPresenceService {
	private final Map<String, Map<String, PresenceMember>> presenceByChannel = new ConcurrentHashMap<>();

	public List<ChannelParticipantResponse> enter(String channelId, AuthUser user) {
		presenceByChannel.computeIfAbsent(channelId, key -> new ConcurrentHashMap<>())
			.put(user.userId(), new PresenceMember(user.userId(), user.displayName(), user.role().name(), LocalDateTime.now()));
		return current(channelId);
	}

	public List<ChannelParticipantResponse> heartbeat(String channelId, AuthUser user) {
		presenceByChannel.computeIfAbsent(channelId, key -> new ConcurrentHashMap<>())
			.computeIfPresent(user.userId(), (key, member) -> member.touch());
		return current(channelId);
	}

	public List<ChannelParticipantResponse> leave(String channelId, AuthUser user) {
		presenceByChannel.computeIfAbsent(channelId, key -> new ConcurrentHashMap<>()).remove(user.userId());
		return current(channelId);
	}

	public List<ChannelParticipantResponse> current(String channelId) {
		purgeExpired(channelId);
		List<ChannelParticipantResponse> result = new ArrayList<>();
		presenceByChannel.getOrDefault(channelId, Map.of()).values().stream()
			.sorted(Comparator.comparing(PresenceMember::displayName))
			.forEach(member -> result.add(member.toResponse()));
		return result;
	}

	private void purgeExpired(String channelId) {
		LocalDateTime threshold = LocalDateTime.now().minusSeconds(45);
		presenceByChannel.getOrDefault(channelId, Map.of()).entrySet().removeIf(entry -> entry.getValue().lastSeen().isBefore(threshold));
	}

	private record PresenceMember(String userId, String displayName, String role, LocalDateTime lastSeen) {
		PresenceMember touch() { return new PresenceMember(userId, displayName, role, LocalDateTime.now()); }
		ChannelParticipantResponse toResponse() { return new ChannelParticipantResponse(userId, displayName, com.aistudy.api.auth.Role.valueOf(role)); }
	}
}
