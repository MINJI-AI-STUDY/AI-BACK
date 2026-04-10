package com.aistudy.api.channel.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.channel.dto.ChannelResponse;
import com.aistudy.api.channel.dto.ChannelWorkspaceResponse;
import com.aistudy.api.channel.dto.CreateChannelRequest;
import com.aistudy.api.channel.dto.UpdateChannelRequest;
import com.aistudy.api.channel.service.ChannelService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/channels")
public class TeacherChannelController {
	private final AuthService authService;
	private final ChannelService channelService;

	public TeacherChannelController(AuthService authService, ChannelService channelService) {
		this.authService = authService;
		this.channelService = channelService;
	}

	@GetMapping
	public List<ChannelResponse> list(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return channelService.list(teacher.schoolId()).stream().map(ChannelResponse::from).toList();
	}

	@PostMapping
	public ChannelResponse create(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @Valid @RequestBody CreateChannelRequest request) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return ChannelResponse.from(channelService.create(teacher, request.name(), request.description(), request.sortOrder()));
	}

	@PatchMapping("/{channelId}")
	public ChannelResponse update(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId, @Valid @RequestBody UpdateChannelRequest request) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return ChannelResponse.from(channelService.update(teacher, channelId, request.name(), request.description(), request.sortOrder(), request.active()));
	}

	@GetMapping("/{channelId}/workspace")
	public ChannelWorkspaceResponse workspace(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return channelService.workspace(teacher, channelId);
	}
}
