package com.aistudy.api.channel.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateChannelRequest(@NotBlank String name, String description, int sortOrder, boolean active) {}
