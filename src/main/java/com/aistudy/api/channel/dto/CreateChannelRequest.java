package com.aistudy.api.channel.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateChannelRequest(@NotBlank String name, String description, int sortOrder) {}
