package com.aistudy.api.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSchoolRequest(@NotBlank String name) {
}
