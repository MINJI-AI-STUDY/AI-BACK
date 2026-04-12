package com.aistudy.api.channel.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.channel.dto.ChannelResponse;
import com.aistudy.api.channel.dto.ChannelWorkspaceResponse;
import com.aistudy.api.channel.dto.CreateChannelRequest;
import com.aistudy.api.channel.dto.UpdateChannelRequest;
import com.aistudy.api.channel.service.ChannelService;
import com.aistudy.api.channel.service.ChannelWorkspaceQueryService;
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

/** 교사 채널 컨트롤러 — 모든 작업은 교사 소속 학교 범위로 제한됩니다. */
@RestController
@RequestMapping("/api/teacher/channels")
public class TeacherChannelController {
	private final AuthService authService;
	private final ChannelService channelService;
	private final ChannelWorkspaceQueryService channelWorkspaceQueryService;

	public TeacherChannelController(AuthService authService, ChannelService channelService, ChannelWorkspaceQueryService channelWorkspaceQueryService) {
		this.authService = authService;
		this.channelService = channelService;
		this.channelWorkspaceQueryService = channelWorkspaceQueryService;
	}

	/** 채널 목록 조회 — 교사 소속 학교의 활성 채널만 반환합니다. */
	@GetMapping
	public List<ChannelResponse> list(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return channelService.list(teacher.schoolId()).stream().map(ChannelResponse::from).toList();
	}

	/** 채널 생성 — 새 채널은 교사 소속 학교에 귀속됩니다. */
	@PostMapping
	public ChannelResponse create(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @Valid @RequestBody CreateChannelRequest request) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return ChannelResponse.from(channelService.create(teacher, request.name(), request.description(), request.sortOrder()));
	}

	/** 채널 수정 — 교사 소속 학교 범위의 채널만 수정할 수 있습니다. */
	@PatchMapping("/{channelId}")
	public ChannelResponse update(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId, @Valid @RequestBody UpdateChannelRequest request) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return ChannelResponse.from(channelService.update(teacher, channelId, request.name(), request.description(), request.sortOrder(), request.active()));
	}

	/** 채널 워크스페이스 조회 — 교사 소속 학교 범위의 채널만 접근할 수 있습니다. */
	@GetMapping("/{channelId}/workspace")
	public ChannelWorkspaceResponse workspace(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String channelId) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return channelWorkspaceQueryService.getWorkspace(teacher, channelId);
	}
}
