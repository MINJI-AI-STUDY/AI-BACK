package com.aistudy.api.channel.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.channel.dto.ChannelResponse;
import com.aistudy.api.channel.dto.ChannelWorkspaceResponse;
import com.aistudy.api.channel.service.ChannelService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/channels")
public class StudentChannelController {
	private final AuthService authService;
	private final ChannelService channelService;

	public StudentChannelController(AuthService authService, ChannelService channelService) {
		this.authService = authService;
		this.channelService = channelService;
	}

	@GetMapping
	public List<ChannelResponse> list(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		AuthUser student = authService.requireRole(authorizationHeader, Role.STUDENT);
		return channelService.list(student.schoolId()).stream().map(ChannelResponse::from).toList();
	}

	@GetMapping("/{channelId}/workspace")
	public ChannelWorkspaceResponse workspace(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId) {
		AuthUser student = authService.requireRole(authorizationHeader, Role.STUDENT);
		return channelService.workspace(student, channelId);
	}
}
