package com.aistudy.api.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSchoolRequest(@NotBlank String name, boolean active) {
}
