package com.aistudy.api.channel.dto;

import com.aistudy.api.auth.Role;

public record ChannelParticipantResponse(String userId, String displayName, Role role) {}
