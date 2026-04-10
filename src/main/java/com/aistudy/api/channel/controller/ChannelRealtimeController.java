package com.aistudy.api.channel.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.channel.dto.ChannelEventResponse;
import com.aistudy.api.channel.dto.ChannelMessageRequest;
import com.aistudy.api.channel.dto.ChannelMessageResponse;
import com.aistudy.api.channel.service.ChannelEventService;
import com.aistudy.api.channel.service.ChannelMessageService;
import com.aistudy.api.channel.service.ChannelPresenceService;
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

@RestController
@RequestMapping("/api/channels")
public class ChannelRealtimeController {
	private final AuthService authService;
	private final ChannelEventService channelEventService;
	private final ChannelPresenceService channelPresenceService;
	private final ChannelMessageService channelMessageService;

	public ChannelRealtimeController(AuthService authService, ChannelEventService channelEventService, ChannelPresenceService channelPresenceService, ChannelMessageService channelMessageService) {
		this.authService = authService;
		this.channelEventService = channelEventService;
		this.channelPresenceService = channelPresenceService;
		this.channelMessageService = channelMessageService;
	}

	@GetMapping("/{channelId}/events")
	public SseEmitter subscribe(@PathVariable String channelId, @RequestParam String accessToken) throws IOException {
		authService.getCurrentUser("Bearer " + accessToken);
		SseEmitter emitter = channelEventService.subscribe(channelId);
		emitter.send(SseEmitter.event().name("ready").data(ChannelEventResponse.presence(channelPresenceService.current(channelId))));
		return emitter;
	}

	@PostMapping("/{channelId}/presence/enter")
	public void enter(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId) {
		AuthUser user = authService.getCurrentUser(authorizationHeader);
		channelEventService.publish(channelId, ChannelEventResponse.presence(channelPresenceService.enter(channelId, user)));
	}

	@PostMapping("/{channelId}/presence/heartbeat")
	public void heartbeat(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId) {
		AuthUser user = authService.getCurrentUser(authorizationHeader);
		channelEventService.publish(channelId, ChannelEventResponse.presence(channelPresenceService.heartbeat(channelId, user)));
	}

	@PostMapping("/{channelId}/presence/leave")
	public void leave(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId) {
		AuthUser user = authService.getCurrentUser(authorizationHeader);
		channelEventService.publish(channelId, ChannelEventResponse.presence(channelPresenceService.leave(channelId, user)));
	}

	@PostMapping("/{channelId}/messages")
	public ChannelMessageResponse sendMessage(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId, @RequestBody ChannelMessageRequest request) {
		AuthUser user = authService.getCurrentUser(authorizationHeader);
		ChannelMessageResponse response = channelMessageService.send(user, channelId, request.content());
		channelEventService.publish(channelId, ChannelEventResponse.message(response));
		return response;
	}
}
