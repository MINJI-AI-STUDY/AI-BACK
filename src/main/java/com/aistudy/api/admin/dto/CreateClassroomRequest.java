package com.aistudy.api.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateClassroomRequest(@NotBlank String name, Integer grade) {
}
