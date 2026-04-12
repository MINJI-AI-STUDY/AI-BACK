package com.aistudy.api.channel.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public record CurateTestChannelsRequest(
	@Size(min = 1, max = 10) List<String> keepNames
) {
	public List<String> normalizedKeepNames() {
		if (keepNames == null || keepNames.isEmpty()) {
			return List.of("테스트 채널 1", "테스트 채널 2");
		}
		return keepNames.stream()
			.map(name -> name == null ? "" : name.trim())
			.filter(name -> !name.isBlank())
			.distinct()
			.toList();
	}
}
