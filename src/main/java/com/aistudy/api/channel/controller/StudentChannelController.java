package com.aistudy.api.channel.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.channel.dto.ChannelResponse;
import com.aistudy.api.channel.dto.ChannelWorkspaceResponse;
import com.aistudy.api.channel.service.ChannelService;
import com.aistudy.api.channel.service.ChannelWorkspaceQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 학생 채널 컨트롤러 — 모든 작업은 학생 소속 학교 범위로 제한됩니다. */
@RestController
@RequestMapping("/api/student/channels")
public class StudentChannelController {
	private final AuthService authService;
	private final ChannelService channelService;
	private final ChannelWorkspaceQueryService channelWorkspaceQueryService;

	public StudentChannelController(AuthService authService, ChannelService channelService, ChannelWorkspaceQueryService channelWorkspaceQueryService) {
		this.authService = authService;
		this.channelService = channelService;
		this.channelWorkspaceQueryService = channelWorkspaceQueryService;
	}

	/** 채널 목록 조회 — 학생 소속 학교의 활성 채널만 반환합니다. */
	@GetMapping
	public List<ChannelResponse> list(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		AuthUser student = authService.requireRole(authorizationHeader, Role.STUDENT);
		return channelService.list(student.schoolId()).stream().map(ChannelResponse::from).toList();
	}

	/** 채널 워크스페이스 조회 — 학생 소속 학교 범위의 채널만 접근할 수 있습니다. */
	@GetMapping("/{channelId}/workspace")
	public ChannelWorkspaceResponse workspace(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId) {
		AuthUser student = authService.requireRole(authorizationHeader, Role.STUDENT);
		return channelWorkspaceQueryService.getWorkspace(student, channelId);
	}
}
