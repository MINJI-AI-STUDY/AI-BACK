package com.aistudy.api.channel.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.channel.dto.ChannelEventResponse;
import com.aistudy.api.channel.dto.ChannelMessageRequest;
import com.aistudy.api.channel.dto.ChannelMessageResponse;
import com.aistudy.api.channel.service.ChannelEventService;
import com.aistudy.api.channel.service.ChannelMessageService;
import com.aistudy.api.channel.service.ChannelPresenceService;
import com.aistudy.api.channel.service.ChannelService;
import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 채널 실시간 컨트롤러 — 모든 작업은 사용자 소속 학교의 채널로 제한됩니다. */
@RestController
@RequestMapping("/api/channels")
public class ChannelRealtimeController {
	private final AuthService authService;
	private final ChannelService channelService;
	private final ChannelEventService channelEventService;
	private final ChannelPresenceService channelPresenceService;
	private final ChannelMessageService channelMessageService;

	public ChannelRealtimeController(AuthService authService, ChannelService channelService, ChannelEventService channelEventService, ChannelPresenceService channelPresenceService, ChannelMessageService channelMessageService) {
		this.authService = authService;
		this.channelService = channelService;
		this.channelEventService = channelEventService;
		this.channelPresenceService = channelPresenceService;
		this.channelMessageService = channelMessageService;
	}

	/** SSE 구독 — 사용자 소속 학교의 채널만 접근 가능합니다. */
	@GetMapping("/{channelId}/events")
	public SseEmitter subscribe(@PathVariable String channelId, @RequestParam String accessToken) throws IOException {
		AuthUser user = authService.getCurrentUser("Bearer " + accessToken);
		channelService.get(user.schoolId(), channelId);
		SseEmitter emitter = channelEventService.subscribe(channelId);
		emitter.send(SseEmitter.event().name("ready").data(ChannelEventResponse.presence(channelPresenceService.current(channelId))));
		return emitter;
	}

	/** 채널 입장 — 사용자 소속 학교의 채널만 접근 가능합니다. */
	@PostMapping("/{channelId}/presence/enter")
	public void enter(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId) {
		AuthUser user = authService.getCurrentUser(authorizationHeader);
		channelService.get(user.schoolId(), channelId);
		channelEventService.publish(channelId, ChannelEventResponse.presence(channelPresenceService.enter(channelId, user)));
	}

	/** 채널 하트비트 — 사용자 소속 학교의 채널만 접근 가능합니다. */
	@PostMapping("/{channelId}/presence/heartbeat")
	public void heartbeat(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId) {
		AuthUser user = authService.getCurrentUser(authorizationHeader);
		channelService.get(user.schoolId(), channelId);
		channelEventService.publish(channelId, ChannelEventResponse.presence(channelPresenceService.heartbeat(channelId, user)));
	}

	/** 채널 퇴장 — 사용자 소속 학교의 채널만 접근 가능합니다. */
	@PostMapping("/{channelId}/presence/leave")
	public void leave(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId) {
		AuthUser user = authService.getCurrentUser(authorizationHeader);
		channelService.get(user.schoolId(), channelId);
		channelEventService.publish(channelId, ChannelEventResponse.presence(channelPresenceService.leave(channelId, user)));
	}

	/** 채널 메시지 전송 — 사용자 소속 학교의 채널만 접근 가능합니다. */
	@PostMapping("/{channelId}/messages")
	public ChannelMessageResponse sendMessage(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId, @RequestBody ChannelMessageRequest request) {
		AuthUser user = authService.getCurrentUser(authorizationHeader);
		channelService.get(user.schoolId(), channelId);
		ChannelMessageResponse response = channelMessageService.send(user, channelId, request.content());
		channelEventService.publish(channelId, ChannelEventResponse.message(response));
		return response;
	}
}