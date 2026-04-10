package com.aistudy.api.channel.dto;

import jakarta.validation.constraints.NotBlank;

public record ChannelMessageRequest(@NotBlank String content) {}
